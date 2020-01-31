package org.watsi.enrollment.device.db.models

import org.junit.Test
import org.watsi.enrollment.device.factories.MemberModelFactory
import java.util.UUID

class MemberModelTest {

    @Test(expected = ModelValidationException::class)
    fun validations_nameCannotBeBlank() {
        MemberModelFactory.build(name = "", householdId = UUID.randomUUID())
    }

    @Test(expected = ModelValidationException::class)
    fun validations_phoneNumberCannotBeBlank() {
        MemberModelFactory.build(phoneNumber = "", householdId = UUID.randomUUID())
    }
}
