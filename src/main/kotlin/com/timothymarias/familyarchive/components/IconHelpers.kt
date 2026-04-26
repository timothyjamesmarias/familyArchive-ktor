package com.timothymarias.familyarchive.components

/**
 * SVG Icon helpers for consistent icon usage across the application.
 * All icons use Heroicons (https://heroicons.com/) for consistency.
 * Icons return raw SVG strings to be used with unsafe { +iconName() }
 */
object IconHelpers {
    /**
     * Back arrow icon - used for navigation back buttons
     */
    fun backArrowIcon(classes: String = "w-5 h-5"): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>"""

    /**
     * Users/group icon - used for user-related features
     */
    fun usersIcon(classes: String = "w-8 h-8"): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>"""

    /**
     * Plus/add icon - used for add/create actions
     */
    fun addIcon(classes: String = "w-6 h-6"): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>"""

    /**
     * Photo/image icon - used for photo artifacts and thumbnails
     */
    fun photoIcon(
        classes: String = "w-8 h-8",
        strokeWidth: String = "2",
    ) =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="$strokeWidth" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>"""

    /**
     * Refresh/reload icon - used for refresh and regenerate actions
     */
    fun refreshIcon(classes: String = "w-5 h-5"): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>"""

    /**
     * Info/information icon - used for status messages and information
     */
    fun infoIcon(
        classes: String = "w-6 h-6",
        id: String? = null,
    ): String {
        val idAttr = id?.let { """id="$it"""" } ?: ""
        return """<svg class="$classes" $idAttr fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>"""
    }

    /**
     * Info circle icon (filled) - used for information boxes
     */
    fun infoCircleFilledIcon(classes: String = "h-5 w-5"): String =
        """<svg class="$classes" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path></svg>"""

    /**
     * Document/file icon - used for file placeholders and document artifacts
     */
    fun documentIcon(
        classes: String = "w-12 h-12",
        strokeWidth: String = "2",
    ): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="$strokeWidth" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"></path></svg>"""

    /**
     * Document with lines icon - used for document artifacts with text
     */
    fun documentTextIcon(
        classes: String = "w-6 h-6",
        strokeWidth: String = "1.5",
    ): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="$strokeWidth" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>"""

    /**
     * Chevron right icon - used for navigation indicators
     */
    fun chevronRightIcon(classes: String = "w-5 h-5"): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>"""

    /**
     * Video icon - used for video artifacts
     */
    fun videoIcon(
        classes: String = "w-6 h-6",
        strokeWidth: String = "1.5",
    ): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="$strokeWidth" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"></path></svg>"""

    /**
     * Audio/microphone icon - used for audio artifacts
     */
    fun audioIcon(
        classes: String = "w-6 h-6",
        strokeWidth: String = "1.5",
    ): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="$strokeWidth" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"></path></svg>"""

    /**
     * Letter/envelope icon - used for letter artifacts
     */
    fun letterIcon(
        classes: String = "w-6 h-6",
        strokeWidth: String = "1.5",
    ): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="$strokeWidth" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg>"""

    /**
     * Ledger/book icon - used for ledger artifacts
     */
    fun ledgerIcon(
        classes: String = "w-6 h-6",
        strokeWidth: String = "1.5",
    ): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="$strokeWidth" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg>"""

    /**
     * Sun icon - used for dark mode toggle (shows in dark mode)
     */
    fun sunIcon(classes: String = "w-5 h-5 text-stone-700 dark:text-stone-300 hidden dark:block"): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>"""

    /**
     * Moon icon - used for dark mode toggle (shows in light mode)
     */
    fun moonIcon(classes: String = "w-5 h-5 text-stone-700 dark:text-stone-300 block dark:hidden"): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"></path></svg>"""

    /**
     * Hamburger menu icon - used for mobile sidebar toggle
     */
    fun hamburgerIcon(classes: String = "w-6 h-6"): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path></svg>"""

    /**
     * Close/X icon - used for closing modals and sidebars
     */
    fun closeIcon(classes: String = "w-6 h-6"): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>"""

    /**
     * Settings/gear icon - used for system utilities and settings
     */
    fun settingsIcon(classes: String = "w-5 h-5"): String =
        """<svg class="$classes" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>"""
}
