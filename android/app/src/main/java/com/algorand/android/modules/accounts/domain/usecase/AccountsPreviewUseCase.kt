/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.algorand.android.modules.accounts.domain.usecase

import com.algorand.android.R
import com.algorand.android.banner.domain.model.BaseBanner
import com.algorand.android.banner.domain.model.BaseBanner.GenericBanner
import com.algorand.android.banner.domain.model.BaseBanner.GovernanceBanner
import com.algorand.android.banner.domain.usecase.BannersUseCase
import com.algorand.android.banner.ui.mapper.BaseBannerItemMapper
import com.algorand.android.core.AccountManager
import com.algorand.android.customviews.accountandassetitem.mapper.AccountItemConfigurationMapper
import com.algorand.android.mapper.AccountPreviewMapper
import com.algorand.android.models.Account
import com.algorand.android.models.AccountDetail
import com.algorand.android.models.AccountIconResource
import com.algorand.android.modules.accounts.domain.mapper.AccountListItemMapper
import com.algorand.android.modules.accounts.domain.mapper.BasePortfolioValueMapper
import com.algorand.android.modules.accounts.domain.model.AccountPreview
import com.algorand.android.modules.accounts.domain.model.AccountValue
import com.algorand.android.modules.accounts.domain.model.BaseAccountListItem
import com.algorand.android.modules.accounts.domain.model.BaseAccountListItem.HeaderItem
import com.algorand.android.modules.accounts.domain.model.BasePortfolioValue
import com.algorand.android.modules.currency.domain.usecase.CurrencyUseCase
import com.algorand.android.modules.parity.domain.model.SelectedCurrencyDetail
import com.algorand.android.modules.parity.domain.usecase.ParityUseCase
import com.algorand.android.modules.sorting.accountsorting.domain.usecase.AccountSortPreferenceUseCase
import com.algorand.android.modules.sorting.accountsorting.domain.usecase.GetSortedAccountsByPreferenceUseCase
import com.algorand.android.nft.domain.usecase.SimpleCollectibleUseCase
import com.algorand.android.tutorialdialog.domain.usecase.AccountAddressTutorialDisplayPreferencesUseCase
import com.algorand.android.usecase.AccountDetailUseCase
import com.algorand.android.usecase.NodeSettingsUseCase
import com.algorand.android.usecase.SimpleAssetDetailUseCase
import com.algorand.android.utils.CacheResult
import com.algorand.android.utils.formatAsCurrency
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

// TODO Refactor this class for performance and code quality
@Suppress("LongParameterList")
class AccountsPreviewUseCase @Inject constructor(
    private val parityUseCase: ParityUseCase,
    private val accountDetailUseCase: AccountDetailUseCase,
    private val assetDetailUseCase: SimpleAssetDetailUseCase,
    private val accountManager: AccountManager,
    private val accountPreviewMapper: AccountPreviewMapper,
    private val accountListItemMapper: AccountListItemMapper,
    private val simpleCollectibleUseCase: SimpleCollectibleUseCase,
    private val bannersUseCase: BannersUseCase,
    private val baseBannerItemMapper: BaseBannerItemMapper,
    private val nodeSettingsUseCase: NodeSettingsUseCase,
    private val basePortfolioValueMapper: BasePortfolioValueMapper,
    private val accountItemConfigurationMapper: AccountItemConfigurationMapper,
    private val accountAddressTutorialDisplayPreferencesUseCase: AccountAddressTutorialDisplayPreferencesUseCase,
    private val getSortedAccountsByPreferenceUseCase: GetSortedAccountsByPreferenceUseCase,
    private val getAccountValueUseCase: GetAccountValueUseCase,
    private val accountSortPreferenceUseCase: AccountSortPreferenceUseCase,
    private val currencyUseCase: CurrencyUseCase
) {

    suspend fun getInitialAccountPreview(): AccountPreview {
        val isTestnetBadgeVisible = nodeSettingsUseCase.isSelectedNodeTestnet()
        return accountPreviewMapper.getFullScreenLoadingState(isTestnetBadgeVisible)
    }

    suspend fun getAccountsPreview(previousState: AccountPreview): Flow<AccountPreview> {
        return combine(
            parityUseCase.getSelectedCurrencyDetailCacheFlow(),
            accountDetailUseCase.getAccountDetailCacheFlow(),
            bannersUseCase.getBanners(),
            nodeSettingsUseCase.getAllNodeAsFlow(),
            assetDetailUseCase.getCachedAssetsFlow()
        ) { selectedCurrencyParityCache, accountDetailCache, banners, _, _ ->
            val isTestnetBadgeVisible = nodeSettingsUseCase.isSelectedNodeTestnet()
            val localAccounts = accountManager.getAccounts()
            if (localAccounts.isEmpty()) {
                return@combine accountPreviewMapper.getEmptyAccountListState(isTestnetBadgeVisible)
            }
            when (selectedCurrencyParityCache) {
                is CacheResult.Success -> {
                    processAccountsAndAssets(accountDetailCache, banners, isTestnetBadgeVisible)
                }
                is CacheResult.Error -> getAlgoPriceErrorState(
                    selectedCurrencyDetailCache = selectedCurrencyParityCache,
                    previousState = previousState,
                    isTestnetBadgeVisible = isTestnetBadgeVisible
                )
                else -> accountPreviewMapper.getFullScreenLoadingState(isTestnetBadgeVisible)
            }
        }
    }

    suspend fun onCloseBannerClick(bannerId: Long) {
        bannersUseCase.dismissBanner(bannerId)
    }

    private suspend fun getAlgoPriceErrorState(
        selectedCurrencyDetailCache: CacheResult.Error<SelectedCurrencyDetail>?,
        previousState: AccountPreview,
        isTestnetBadgeVisible: Boolean
    ): AccountPreview {
        val hasPreviousCachedValue = selectedCurrencyDetailCache?.data != null
        if (hasPreviousCachedValue) return previousState
        val accountErrorListItems = createAccountErrorItemList()
        val portfolioValuesError = basePortfolioValueMapper.mapToPortfolioValuesInitializationError()
        return accountPreviewMapper.getAlgoPriceInitialErrorState(
            accountListItems = accountErrorListItems,
            errorCode = selectedCurrencyDetailCache?.code,
            isTestnetBadgeVisible = isTestnetBadgeVisible,
            portfolioValuesError = portfolioValuesError
        )
    }

    private suspend fun processAccountsAndAssets(
        accountDetailCache: HashMap<String, CacheResult<AccountDetail>>,
        banners: List<BaseBanner>,
        isTestnetBadgeVisible: Boolean
    ): AccountPreview {
        val areAllAccountsAreCached = accountDetailUseCase.areAllAccountsCached()
        return if (areAllAccountsAreCached) {
            processSuccessAccountCacheAndOthers(accountDetailCache, banners, isTestnetBadgeVisible)
        } else {
            accountPreviewMapper.getFullScreenLoadingState(isTestnetBadgeVisible)
        }
    }

    private suspend fun processSuccessAccountCacheAndOthers(
        accountDetailCache: HashMap<String, CacheResult<AccountDetail>>,
        banners: List<BaseBanner>,
        isTestnetBadgeVisible: Boolean
    ): AccountPreview {
        val isThereAnyAssetNeedsToBeCached = accountDetailCache.values.any {
            !it.data?.accountInformation?.assetHoldingList.isNullOrEmpty()
        }
        return if (
            assetDetailUseCase.getCachedAssetList().isEmpty() &&
            simpleCollectibleUseCase.getCachedCollectibleList().isEmpty() &&
            isThereAnyAssetNeedsToBeCached
        ) {
            accountPreviewMapper.getFullScreenLoadingState(isTestnetBadgeVisible)
        } else {
            prepareAccountPreview(accountDetailCache, banners, isTestnetBadgeVisible)
        }
    }

    private suspend fun prepareAccountPreview(
        accountDetailCache: HashMap<String, CacheResult<AccountDetail>>,
        banners: List<BaseBanner>,
        isTestnetBadgeVisible: Boolean
    ): AccountPreview {
        return withContext(Dispatchers.Default) {
            var primaryAccountValue = BigDecimal.ZERO
            var secondaryAccountValue = BigDecimal.ZERO

            val baseAccountListItems = getBaseAccountListItems(onAccountValueCalculated = {
                primaryAccountValue += it.primaryAccountValue
                secondaryAccountValue += it.secondaryAccountValue
            }).apply {
                val banner = getBannerItemOrNull(banners)
                insertQuickActionsItem(this)
                if (banner != null) add(BANNER_ITEM_INDEX, banner)
            }
            val isThereAnyErrorInAccountCache = accountDetailCache.any {
                it.value is CacheResult.Error<*> && it.value.data == null
            }
            val portfolioValueErrorItem = if (isThereAnyErrorInAccountCache) {
                basePortfolioValueMapper.mapToPortfolioValuesPartialError()
            } else {
                null
            }
            val portfolioValueItem = if (!isThereAnyErrorInAccountCache) {
                getPortfolioValueItem(primaryAccountValue, secondaryAccountValue)
            } else {
                null
            }
            val shouldShowTutorialDialog = accountAddressTutorialDisplayPreferencesUseCase.shouldShowTutorialDialog()

            accountPreviewMapper.getSuccessAccountPreview(
                accountListItems = baseAccountListItems,
                isTestnetBadgeVisible = isTestnetBadgeVisible,
                portfolioValues = portfolioValueItem,
                portfolioValuesError = portfolioValueErrorItem,
                shouldShowDialog = shouldShowTutorialDialog
            )
        }
    }

    fun setTutorialPreferences(isShown: Boolean) {
        accountAddressTutorialDisplayPreferencesUseCase.setTutorialPreferences(isShown)
    }

    private fun insertQuickActionsItem(accountsList: MutableList<BaseAccountListItem>) {
        accountsList.add(QUICK_ACTIONS_ITEM_INDEX, accountListItemMapper.mapToQuickActionsItem())
    }

    private fun getBannerItemOrNull(bannerList: List<BaseBanner>): BaseAccountListItem.BaseBannerItem? {
        return bannerList.firstOrNull()?.let { banner ->
            val isButtonVisible = !banner.buttonTitle.isNullOrBlank() && !banner.buttonUrl.isNullOrBlank()
            val isTitleVisible = !banner.title.isNullOrBlank()
            val isDescriptionVisible = !banner.description.isNullOrBlank()
            with(baseBannerItemMapper) {
                when (banner) {
                    is GovernanceBanner -> {
                        mapToGovernanceBannerItem(banner, isButtonVisible, isTitleVisible, isDescriptionVisible)
                    }
                    is GenericBanner -> {
                        mapToGenericBannerItem(banner, isButtonVisible, isTitleVisible, isDescriptionVisible)
                    }
                }
            }
        }
    }

    private suspend fun getBaseAccountListItems(
        onAccountValueCalculated: (AccountValue) -> Unit
    ): MutableList<BaseAccountListItem> {
        val selectedCurrencySymbol = parityUseCase.getPrimaryCurrencySymbolOrEmpty()
        val secondaryCurrencySymbol = parityUseCase.getSecondaryCurrencySymbol()
        val isPrimaryCurrencyAlgo = currencyUseCase.isPrimaryCurrencyAlgo()
        val isSecondaryCurrencyAlgo = !isPrimaryCurrencyAlgo
        val sortedAccountListItems = getSortedAccountsByPreferenceUseCase.getSortedAccountListItems(
            sortingPreferences = accountSortPreferenceUseCase.getAccountSortPreference(),
            onLoadedAccountConfiguration = {
                val accountBalance = getAccountValueUseCase.getAccountValue(this).also { accountValue ->
                    if (account.type != Account.Type.WATCH) {
                        onAccountValueCalculated.invoke(accountValue)
                    }
                }
                accountItemConfigurationMapper.mapTo(
                    accountAddress = account.address,
                    accountName = account.name,
                    accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(account.type),
                    accountType = account.type,
                    accountPrimaryValueText = accountBalance.primaryAccountValue.formatAsCurrency(
                        symbol = selectedCurrencySymbol,
                        isCompact = true,
                        isFiat = !isPrimaryCurrencyAlgo
                    ),
                    accountSecondaryValueText = accountBalance.secondaryAccountValue.formatAsCurrency(
                        symbol = secondaryCurrencySymbol,
                        isCompact = true,
                        isFiat = !isSecondaryCurrencyAlgo
                    ),
                    accountPrimaryValue = accountBalance.primaryAccountValue,
                    accountSecondaryValue = accountBalance.secondaryAccountValue
                )
            }, onFailedAccountConfiguration = {
                this?.run {
                    accountItemConfigurationMapper.mapTo(
                        accountAddress = address,
                        accountName = name,
                        accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(type),
                        showWarningIcon = true
                    )
                }
            }
        )
        val baseAccountList = sortedAccountListItems.map { accountListItem ->
            if (accountListItem.itemConfiguration.showWarning == true) {
                accountListItemMapper.mapToErrorAccountItem(
                    accountListItem = accountListItem,
                    canCopyable = accountListItem.itemConfiguration.accountType != Account.Type.WATCH
                )
            } else {
                accountListItemMapper.mapToAccountItem(
                    accountListItem = accountListItem,
                    canCopyable = accountListItem.itemConfiguration.accountType != Account.Type.WATCH
                )
            }
        }
        return mutableListOf<BaseAccountListItem>().apply {
            if (baseAccountList.isNotEmpty()) {
                add(HeaderItem(R.string.accounts))
                addAll(baseAccountList)
            }
        }
    }

    private fun getPortfolioValueItem(
        primaryAccountValue: BigDecimal,
        secondaryAccountValue: BigDecimal
    ): BasePortfolioValue.PortfolioValues {
        val selectedCurrencySymbol = parityUseCase.getPrimaryCurrencySymbolOrName()
        val secondaryCurrencySymbol = parityUseCase.getSecondaryCurrencySymbol()
        return basePortfolioValueMapper.mapToPortfolioValuesSuccess(
            formattedPrimaryAccountValue = primaryAccountValue.formatAsCurrency(selectedCurrencySymbol),
            formattedSecondaryAccountValue = secondaryAccountValue.formatAsCurrency(secondaryCurrencySymbol)
        )
    }

    private suspend fun createAccountErrorItemList(): List<BaseAccountListItem> {
        val sortedAccountListItems = getSortedAccountsByPreferenceUseCase.getSortedAccountListItems(
            sortingPreferences = accountSortPreferenceUseCase.getAccountSortPreference(),
            onLoadedAccountConfiguration = {
                accountItemConfigurationMapper.mapTo(
                    accountAddress = account.address,
                    accountName = account.name,
                    accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(account.type),
                    accountType = account.type,
                    showWarningIcon = true
                )
            },
            onFailedAccountConfiguration = {
                this?.run {
                    accountItemConfigurationMapper.mapTo(
                        accountAddress = address,
                        accountName = name,
                        accountIconResource = AccountIconResource.getAccountIconResourceByAccountType(type),
                        accountType = type,
                        showWarningIcon = true
                    )
                }
            }
        )
        if (sortedAccountListItems.isEmpty()) return emptyList()
        return mutableListOf<BaseAccountListItem>().apply {
            add(HeaderItem(R.string.accounts))
            val baseAccountList = sortedAccountListItems.map { accountListItem ->
                if (accountListItem.itemConfiguration.showWarning == true) {
                    accountListItemMapper.mapToErrorAccountItem(
                        accountListItem = accountListItem,
                        canCopyable = accountListItem.itemConfiguration.accountType != Account.Type.WATCH
                    )
                } else {
                    accountListItemMapper.mapToAccountItem(
                        accountListItem = accountListItem,
                        canCopyable = accountListItem.itemConfiguration.accountType != Account.Type.WATCH
                    )
                }
            }
            addAll(baseAccountList)
            insertQuickActionsItem(this)
        }
    }

    companion object {
        private const val QUICK_ACTIONS_ITEM_INDEX = 0
        private const val BANNER_ITEM_INDEX = 1
    }
}