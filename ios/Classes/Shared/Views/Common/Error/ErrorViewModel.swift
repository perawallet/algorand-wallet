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

//
//   ErrorViewModel.swift

import Foundation
import MacaroonUIKit

protocol ErrorViewModel: ViewModel {
    var icon: Image? { get }
    var message: MessageTextProvider? { get }

    typealias MessageTextProvider = ErrorMessageTextProvider
    typealias HighlightedText = ErrorMessageTextProvider.HighlightedText
}

struct ErrorMessageTextProvider {
    var text: TextProvider
    var highlightedText: HighlightedText? = nil

    struct HighlightedText {
        let text: String
        var url: URL? = nil
        let attributes: TextAttributeGroup
    }
}

extension ErrorViewModel {
    func getIcon() -> Image {
        return "icon-red-warning"
    }
    
    func getMessage(
        _  aMessage: String?
    ) -> MessageTextProvider? {
        guard let aMessage = aMessage else {
            return nil
        }
        
        return MessageTextProvider(text: aMessage.bodyMedium())
    }
}
