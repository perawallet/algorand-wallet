// Copyright 2022 Pera Wallet, LDA

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//   OptOutBlockchainUpdate.swift

import Foundation

struct OptOutBlockchainUpdate: BlockchainUpdate {
    let accountAddress: String
    let assetID: AssetID
    let assetName: String?
    let assetUnitName: String?
    let assetVerificationTier: AssetVerificationTier
    let isCollectibleAsset: Bool
    let status: Status
    let notificationMessage: String

    init(request: OptOutBlockchainRequest) {
        self.accountAddress = request.accountAddress
        self.assetID = request.assetID
        self.assetName = request.assetName
        self.assetUnitName = request.assetUnitName
        self.assetVerificationTier = request.assetVerificationTier
        self.isCollectibleAsset = request.isCollectibleAsset
        self.status = .pending

        let name = request.assetName ?? request.assetUnitName ?? String(request.assetID)
        self.notificationMessage = "asset-opt-out-successful-message".localized(name)
    }

    init(
        update: OptOutBlockchainUpdate,
        status: Status
    ) {
        self.accountAddress = update.accountAddress
        self.assetID = update.assetID
        self.assetName = update.assetName
        self.assetUnitName = update.assetUnitName
        self.assetVerificationTier = update.assetVerificationTier
        self.isCollectibleAsset = update.isCollectibleAsset
        self.status = status
        self.notificationMessage = update.notificationMessage
    }
}

extension OptOutBlockchainUpdate {
    enum Status {
        case pending
        case waitingForNotification
    }
}