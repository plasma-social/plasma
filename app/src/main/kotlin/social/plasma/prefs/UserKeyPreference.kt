package social.plasma.prefs

import social.plasma.prefs.StringPreference.StringPreferenceFactory
import javax.inject.Inject

class UserKeyPreference @Inject constructor(
    stringPreferenceFactory: StringPreferenceFactory,
) : Preference<String> by stringPreferenceFactory.create("plasmakey")
