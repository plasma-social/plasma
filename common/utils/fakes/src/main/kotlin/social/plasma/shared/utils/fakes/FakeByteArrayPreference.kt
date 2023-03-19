package social.plasma.shared.utils.fakes

import social.plasma.shared.utils.api.Preference

class FakeByteArrayPreference(
    private var value: ByteArray? = null,
) : Preference<ByteArray> by FakePreference(value)
