package com.lura.ui.library

enum class ViewMode {
    GRID,
    LIST
}

enum class SortOption(val displayName: String) {
    RECENTLY_OPENED("Recently Opened"),
    TITLE_AZ("Title (A-Z)"),
    AUTHOR_AZ("Author (A-Z)"),
    PROGRESS_DESC("Progress (High to Low)"),
    PROGRESS_ASC("Progress (Low to High)"),
    DATE_ADDED("Date Added"),
    TIME_TO_FINISH("Time to Finish")
}
