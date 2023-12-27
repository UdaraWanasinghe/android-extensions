package com.aureusapps.android.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

/**
 * A custom implementation of ViewModelProvider.Factory that wraps around a parent factory
 * and an array of ViewModel instances to create ViewModels based on their classes.
 *
 * @property parentFactory The parent factory used to create ViewModels if not found in the provided array.
 * @property viewModels An array of ViewModel instances to be used for ViewModel creation.
 */
class ViewModelProviderFactoryWrapper(
    private val parentFactory: ViewModelProvider.Factory,
    vararg viewModels: ViewModel
) : ViewModelProvider.Factory {

    private val viewModels = viewModels.toList()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        for (viewModel in viewModels) {
            if (modelClass.isAssignableFrom(viewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return viewModel as T
            }
        }
        return parentFactory.create(modelClass)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        for (viewModel in viewModels) {
            if (modelClass.isAssignableFrom(viewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return viewModel as T
            }
        }
        return parentFactory.create(modelClass, extras)
    }

}