package arivihan.technologies.doubtbuzzter2.compose

sealed interface ArivihanScreenState<out T> {
    data object Loading : ArivihanScreenState<Nothing>
    data class Content<T>(val data: T) : ArivihanScreenState<T>
    data object NetworkError : ArivihanScreenState<Nothing>
    data class Error(val message: String? = null) : ArivihanScreenState<Nothing>
}
