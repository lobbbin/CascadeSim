package com.cascadesim.core.util

// Re-export Result from common module for backward compatibility
@Deprecated("Use com.cascadesim.common.util.Result instead", ReplaceWith("Result", "com.cascadesim.common.util.Result"))
typealias Result<T> = com.cascadesim.common.util.Result<T>
