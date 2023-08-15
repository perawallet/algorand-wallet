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

//   RekeyInstructionsScreenTheme.swift

import Foundation
import MacaroonUIKit
import UIKit

struct RekeyInstructionsScreenTheme:
    LayoutSheet,
    StyleSheet {
    var background: ViewStyle
    var illustration: ImageStyle
    var illustrationMaxHeight: LayoutMetric
    var illustrationMinHeight: LayoutMetric
    var title: TextStyle
    var titleTopInset: LayoutMetric
    var titleHorizontalEdgeInsets: NSDirectionalHorizontalEdgeInsets
    var spacingBetweenTitleAndBody: LayoutMetric
    var body: TextStyle
    var bodyHorizontalEdgeInsets: NSDirectionalHorizontalEdgeInsets
    var spacingBetweenBodyAndInstructionsTitle: LayoutMetric
    var instructionsTitle: TextStyle
    var spacingBetweenInstructionTitleAndInstructions: LayoutMetric
    var instruction: InstructionItemViewTheme
    var instructionsSpacing: LayoutMetric
    var instructionsHorizontalEdgeInsets: NSDirectionalHorizontalEdgeInsets
    var primaryAction: ButtonStyle
    var primaryActionContentEdgeInsets: UIEdgeInsets
    var primaryActionEdgeInsets: NSDirectionalEdgeInsets

    init(_ family: LayoutFamily) {
        self.background = [
            .backgroundColor(Colors.Defaults.background)
        ]
        self.illustration = [
            .contentMode(.scaleAspectFill)
        ]
        self.illustrationMaxHeight = 180
        self.illustrationMinHeight = 132
        self.title = [
            .textColor(Colors.Text.main),
            .textOverflow(FittingText())
        ]
        self.titleTopInset = 40
        self.titleHorizontalEdgeInsets = .init(
            leading: 24,
            trailing: 24
        )
        self.spacingBetweenTitleAndBody = 16
        self.body = [
            .textColor(Colors.Text.gray),
            .textOverflow(FittingText())
        ]
        self.bodyHorizontalEdgeInsets = .init(
            leading: 24,
            trailing: 24
        )
        self.spacingBetweenBodyAndInstructionsTitle = 40
        self.instructionsTitle = [
            .textColor(Colors.Text.gray),
            .textOverflow(SingleLineText()),
        ]
        self.spacingBetweenInstructionTitleAndInstructions = 16
        self.instruction = InstructionItemViewTheme(family)
        self.instructionsSpacing = 24
        self.instructionsHorizontalEdgeInsets = .init(
            leading: 24,
            trailing: 24
        )
        self.primaryAction = [
            .titleColor([ .normal(Colors.Button.Primary.text) ]),
            .font(Typography.bodyMedium()),
            .backgroundImage([
                .normal("components/buttons/primary/bg"),
                .highlighted("components/buttons/primary/bg-highlighted"),
            ])
        ]
        self.primaryActionContentEdgeInsets = .init(
            top: 16,
            left: 0,
            bottom: 16,
            right: 0
        )
        self.primaryActionEdgeInsets = .init(
            top: 36,
            leading: 24,
            bottom: 16,
            trailing: 24
        )
    }
}
