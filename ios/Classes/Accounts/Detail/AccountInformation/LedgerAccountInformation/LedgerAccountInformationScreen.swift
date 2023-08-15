// Copyright 2023 Pera Wallet, LDA

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//   LedgerAccountInformationScreen.swift

import Foundation
import UIKit
import MacaroonUIKit
import MacaroonBottomSheet

final class LedgerAccountInformationScreen:
    MacaroonUIKit.ScrollScreen,
    BottomSheetScrollPresentable {
    typealias EventHandler = (Event) -> Void
    var eventHandler: EventHandler?

    var modalHeight: ModalHeight {
        return .compressed
    }

    private lazy var contextView = UIView()
    private lazy var titleView = UILabel()
    private lazy var accountItemCanvasView = TripleShadowView()
    private lazy var accountItemView = AccountListItemWithActionView()
    private lazy var accountTypeInformationView = AccountTypeInformationView()
    private lazy var optionsView = AccountInformationOptionsView()

    private let account: Account
    private let copyToClipboardController: CopyToClipboardController

    private lazy var theme = LedgerAccountInformationScreenTheme()

    init(
        account: Account,
        copyToClipboardController: CopyToClipboardController
    ) {
        self.account = account
        self.copyToClipboardController = copyToClipboardController
    }

    override func configureNavigationBar() {
        super.configureNavigationBar()

        navigationBarController.isNavigationBarHidden = true
    }

    override func prepareLayout() {
        super.prepareLayout()

        addContext()
    }
}

extension LedgerAccountInformationScreen {
    private func addContext() {
        contentView.addSubview(contextView)

        contextView.snp.makeConstraints {
            $0.top == theme.contextEdgeInsets.top
            $0.leading == theme.contextEdgeInsets.leading
            $0.trailing == theme.contextEdgeInsets.trailing
            $0.bottom == theme.contextEdgeInsets.bottom
        }

        addTitle()
        addAccountItem()
        addAccountTypeInformation()
        addOptions()
    }

    private func addTitle() {
        contextView.addSubview(titleView)
        titleView.customizeAppearance(theme.title)

        titleView.fitToIntrinsicSize()
        titleView.snp.makeConstraints {
            $0.top == 0
            $0.leading == 0
            $0.trailing == 0
        }

        bindTitle()
    }

    private func addAccountItem() {
        accountItemCanvasView.drawAppearance(shadow: theme.accountItemFirstShadow)
        accountItemCanvasView.drawAppearance(secondShadow: theme.accountItemSecondShadow)
        accountItemCanvasView.drawAppearance(thirdShadow: theme.accountItemThirdShadow)

        contextView.addSubview(accountItemCanvasView)
        accountItemCanvasView.snp.makeConstraints {
            $0.top == titleView.snp.bottom + theme.spacingBetweenTitleAndAccountItem
            $0.leading == 0
            $0.trailing == 0
        }

        accountItemView.customize(theme.accountItem)
        accountItemCanvasView.addSubview(accountItemView)
        accountItemView.snp.makeConstraints {
            $0.top == 0
            $0.leading == 0
            $0.bottom == 0
            $0.trailing == 0
            $0.greaterThanHeight(theme.accountItemMinHeight)
        }

        accountItemView.startObserving(event: .performAction) {
            [unowned self] in
            self.copyToClipboardController.copyAddress(self.account)
        }

        bindAccountItem()
    }

    private func addAccountTypeInformation() {
        accountTypeInformationView.customize(theme.accountTypeInformation)

        contextView.addSubview(accountTypeInformationView)
        accountTypeInformationView.snp.makeConstraints {
            $0.top == accountItemView.snp.bottom + theme.spacingBetweenAccountItemAndAccountTypeInformation
            $0.leading == 0
            $0.trailing == 0
        }

        accountTypeInformationView.startObserving(event: .performHyperlinkAction) {
            [unowned self] in
            self.open(AlgorandWeb.rekey.link)
        }

        bindAccountTypeInformation()
    }

    private func addOptions() {
        optionsView.customize(theme.options)

        contextView.addSubview(optionsView)
        optionsView.snp.makeConstraints {
            $0.top == accountTypeInformationView.snp.bottom + theme.spacingBetweenAccountTypeInformationAndOptions
            $0.leading == 0
            $0.bottom == 0
            $0.trailing == 0
        }

        let options = [
            makeRekeyToLedgerAccountItem(),
            makeRekeyToStandardAccountItem()
        ]
        options.forEach(optionsView.addOption)
    }
}

extension LedgerAccountInformationScreen {
    private func makeRekeyToLedgerAccountItem() -> AccountInformationOptionItem {
        return AccountInformationOptionItem(viewModel: .rekeyToLedger) {
            [unowned self] in
            self.eventHandler?(.performRekeyToLedger)
        }
    }

    private func makeRekeyToStandardAccountItem() -> AccountInformationOptionItem {
        return AccountInformationOptionItem(viewModel: .rekeyToStandard) {
            [unowned self] in
            self.eventHandler?(.performRekeyToStandard)
        }
    }
}

extension LedgerAccountInformationScreen {
    private func bindTitle() {
        titleView.attributedText =
            "title-ledger-account-capitalized-sentence"
                .localized
                .titleSmallMedium(lineBreakMode: .byTruncatingTail)
    }

    private func bindAccountItem() {
        let viewModel = AccountInformationCopyAccountItemViewModel(account)
        accountItemView.bindData(viewModel)
    }

    private func bindAccountTypeInformation() {
        let viewModel = LedgerAccountTypeInformationViewModel()
        accountTypeInformationView.bindData(viewModel)
    }
}

extension LedgerAccountInformationScreen {
    enum Event {
        case performRekeyToLedger
        case performRekeyToStandard
    }
}
