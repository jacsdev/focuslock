package com.developermind.focuslock.ui.admin

sealed class CityEditState {
    object Idle : CityEditState()
    object Saving : CityEditState()
    object Success : CityEditState()
    object Error : CityEditState()
    object ConfirmDelete : CityEditState()
}
