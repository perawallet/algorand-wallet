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

package com.algorand.android.modules.accountblockpolling.domain.usecase

import com.algorand.android.modules.accountblockpolling.domain.repository.AccountBlockPollingRepository
import com.algorand.android.usecase.GetLocalAccountsUseCase
import com.algorand.android.utils.DataResource
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.flow

class GetResultWhetherAccountsUpdateIsRequiredUseCase @Inject constructor(
    private val getLastKnownAccountBlockNumberUseCase: GetLastKnownAccountBlockNumberUseCase,
    private val getLocalAccountsUseCase: GetLocalAccountsUseCase,
    @Named(AccountBlockPollingRepository.INJECTION_NAME)
    private val accountBlockPollingRepository: AccountBlockPollingRepository
) {

    operator fun invoke() = flow<DataResource<Boolean>> {
        val lastKnownBlockRound = getLastKnownAccountBlockNumberUseCase.invoke()?.data
        val accountAddresses = getLocalAccountsUseCase.getLocalAccountsFromAccountManagerCache().map { it.address }
        accountBlockPollingRepository.getResultWhetherAccountsUpdateIsRequired(
            localAccountAddresses = accountAddresses,
            latestKnownRound = lastKnownBlockRound
        ).use(
            onSuccess = { emit(DataResource.Success(it.shouldRefresh == true)) },
            onFailed = { exception, code -> emit(DataResource.Error.Api(exception, code)) }
        )
    }
}
