package org.watsi.enrollment.domain.relations

import org.watsi.enrollment.domain.entities.Member
import org.watsi.enrollment.domain.entities.Photo

data class MemberWithThumbnail(val member: Member, val photo: Photo?) {
    companion object {
        fun asSortedListWithHeadOfHouseholdsFirst(members: List<MemberWithThumbnail>): List<MemberWithThumbnail> {
            val headsOfHousehold = members
                    .filter { it.member.isHeadOfHousehold() }
                    .sortedBy { it.member.enrolledAt }
            val beneficiaries = (members - headsOfHousehold).sortedBy { it.member.enrolledAt }
            return headsOfHousehold + beneficiaries
        }
    }
}
