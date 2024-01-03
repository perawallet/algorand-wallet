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

package com.algorand.android.models

import com.algorand.android.utils.toShortenedAddress

sealed class BaseWalletConnectDisplayedAddress {

    abstract val displayValue: String
    abstract val fullAddress: String
    abstract val isSingleLine: Boolean

    data class ShortenedAddress(override val displayValue: String, override val fullAddress: String) :
        BaseWalletConnectDisplayedAddress() {
        override val isSingleLine: Boolean = true
    }

    data class CustomName(override val displayValue: String, override val fullAddress: String) :
        BaseWalletConnectDisplayedAddress() {
        override val isSingleLine: Boolean = true
    }

    data class FullAddress(override val displayValue: String) : BaseWalletConnectDisplayedAddress() {
        override val isSingleLine: Boolean = false
        override val fullAddress = displayValue
    }

    companion object {
        fun create(
            decodedAddress: String,
            account: WalletConnectAccount?
        ): BaseWalletConnectDisplayedAddress {
            val isDecodedAddressUsersAddress = account?.address == decodedAddress
            return when {
                account == null || !isDecodedAddressUsersAddress -> FullAddress(decodedAddress)
                account.name.isNullOrBlank().not() -> CustomName(account.name, decodedAddress)
                else -> ShortenedAddress(decodedAddress.toShortenedAddress(), decodedAddress)
            }
        }
    }
}
