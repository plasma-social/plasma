package social.plasma.ui
// Taken from https://github.com/slackhq/circuit/blob/499ddbf0ac105466c5e270741b1ca37aa8da684c/samples/star/src/main/kotlin/com/slack/circuit/star/ui/StableCoroutineScope.kt#L17
// Copyright (C) 2023 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope

/**
 * Returns a [StableCoroutineScope] around a [rememberCoroutineScope]. This is useful for event
 * callback lambdas that capture a local scope variable to launch new coroutines, as it allows them
 * to be stable.
 */
@Composable
fun rememberStableCoroutineScope(): StableCoroutineScope {
    val scope = rememberCoroutineScope()
    return remember { StableCoroutineScope(scope) }
}

/** @see rememberStableCoroutineScope */
@Stable
class StableCoroutineScope(scope: CoroutineScope) : CoroutineScope by scope
