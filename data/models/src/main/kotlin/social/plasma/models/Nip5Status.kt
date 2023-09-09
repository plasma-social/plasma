package social.plasma.models

sealed interface Nip5Status {
    object Missing : Nip5Status

    sealed interface Set : Nip5Status {
        val identifier: String

        data class Loading(override val identifier: String) :
            Set

        data class Invalid(override val identifier: String) :
            Set

        data class Valid(override val identifier: String) :
            Set
    }

    fun isValid() = this is Nip5Status.Set.Valid
}
