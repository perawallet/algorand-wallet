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

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

// TODO: 30.12.2021 We must create and use another class for UI layer instead of entity class
@Parcelize
@Entity(indices = [Index(value = ["public_key"], unique = true)])
data class User(
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "public_key")
    val publicKey: String,

    @ColumnInfo(name = "uri")
    val imageUriAsString: String?,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var contactDatabaseId: Int = 0
) : Parcelable {

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<User> = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.publicKey == newItem.publicKey
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}
