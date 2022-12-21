@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.abc.keyboard2023i1

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.View
import androidx.annotation.CallSuper
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner


@Composable
fun CustomKeyboard(imeService: MyIMEService) {

    var isShow by remember { mutableStateOf(true)}

    Column {

        if(isShow) { Text(text = "Good Luck", color = Color.Green) }

        Button(onClick = {
            isShow = !isShow
            imeService.doSomethingWith("A")
        }) {
            Text(text = "Test Show/Hide")
        }
    }
}

class ComposeKeyboardView(private val imeService: MyIMEService) : AbstractComposeView(imeService) {

    @Composable
    override fun Content() {
        CustomKeyboard(imeService)
    }
}


class MyIMEService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    override fun onCreateInputView(): View {
        val view = ComposeKeyboardView(this)

        window!!.window!!.decorView.let { decorView ->
            ViewTreeLifecycleOwner.set(decorView, this)
            ViewTreeViewModelStoreOwner.set(decorView, this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
        return view
    }


    fun doSomethingWith(mData: String) {
        currentInputConnection?.commitText(mData, 1)
    }

    private val _lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }
    private val _store by lazy { ViewModelStore() }

    override fun getLifecycle(): Lifecycle = _lifecycleRegistry

    override fun getViewModelStore(): ViewModelStore = _store

    override val savedStateRegistry: SavedStateRegistry = SavedStateRegistryController.create(this).savedStateRegistry

    private fun handleLifecycleEvent(event: Lifecycle.Event) =
        _lifecycleRegistry.handleLifecycleEvent(event)

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        // You must call performAttach() before calling performRestore(Bundle)
        savedStateRegistry.performAttach(lifecycle)
        savedStateRegistry.performRestore(null)
        handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    @CallSuper
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

