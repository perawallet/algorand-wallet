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

package com.algorand.android.ui.wctransactionrequest.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.algorand.android.databinding.ItemWalletConnectSingleRequestBinding
import com.algorand.android.ui.common.walletconnect.WalletConnectTransactionSummaryCardView
import com.algorand.android.ui.wctransactionrequest.WalletConnectTransactionListItem
import com.algorand.android.ui.wctransactionrequest.WalletConnectTransactionListItem.SingleTransactionItem

class WalletConnectSingleRequestViewHolder(
    private val binding: ItemWalletConnectSingleRequestBinding,
    private val listener: WalletConnectTransactionSummaryCardView.OnShowDetailClickListener
) : BaseWalletConnectTransactionViewHolder(binding.root) {

    override fun bind(item: WalletConnectTransactionListItem) {
        if (item !is SingleTransactionItem) return
        binding.summaryCardView.initTransaction(item, listener)
    }

    companion object {
        fun create(
            parent: ViewGroup,
            listener: WalletConnectTransactionSummaryCardView.OnShowDetailClickListener
        ): WalletConnectSingleRequestViewHolder {
            val binding = ItemWalletConnectSingleRequestBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            return WalletConnectSingleRequestViewHolder(binding, listener)
        }
    }
}
