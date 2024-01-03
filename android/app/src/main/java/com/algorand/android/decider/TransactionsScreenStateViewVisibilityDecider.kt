/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License
 *
 */

package com.algorand.android.decider

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import javax.inject.Inject

class TransactionsScreenStateViewVisibilityDecider @Inject constructor() {

    fun decideScreenStateViewVisibility(
        combinedLoadStates: CombinedLoadStates,
        itemCount: Int,
        isLastStateError: Boolean
    ): Boolean {
        val isLoading = combinedLoadStates.refresh is LoadState.Loading
        val isCurrentStateError = combinedLoadStates.refresh is LoadState.Error
        val isEmptyAfterLoading = isLoading.not() && itemCount == 0 && isCurrentStateError.not()
        return isEmptyAfterLoading || isCurrentStateError || (isLoading && isLastStateError)
    }
}
