package com.sami.pstudocscanner.util

class Constants {
    companion object {
        const val ALL = "All"
        const val DATA = "AppPrefs"
        const val THEME_NAME = "themeName"
        const val PRO_SCANNER = "PSTU Document Scanner"
        const val SELECTED_FILES = "SelectedFile"
        const val SELECTED_FILE_NAME = "SelectedFileName"
        const val IS_ONBOARDED = "isOnboarded"
        const val IS_SWIPE_TO_DELETE_ENABLE = "isSwipeToDeleteEnable"
        const val CATEGORY_LIST_ITEMS = "categoryListItems"
        val DEFAULT_CATEGORY_LIST = listOf(
            "Personal",
            "Finance",
            "Education",
            "Business",
            "Work",
            "Other"
        )
    }
}