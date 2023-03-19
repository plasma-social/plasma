package social.plasma.shared.repositories.real

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class UserKey(val type: KeyType)

enum class KeyType {
    Public, Secret
}
