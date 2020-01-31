package org.watsi.enrollment.domain.relations

import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.Photo

data class MemberWithRawPhoto(val member: Member, val photo: Photo)
