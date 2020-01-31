package org.watsi.enrollment.managers

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.enrollment.fragments.NewHouseholdFragment

@RunWith(MockitoJUnitRunner::class)
class NavigationManagerTest {

    @Mock lateinit var fragmentManager: FragmentManager
    @Mock lateinit var transaction: FragmentTransaction
    val fragmentContainerId = 0
    val fragment = Fragment()
    lateinit var navigationManager: NavigationManager

    @Before
    fun setup() {
        whenever(fragmentManager.beginTransaction()).thenReturn(transaction)
        whenever(transaction.add(anyInt(), any())).thenReturn(transaction)
        whenever(transaction.replace(anyInt(), any())).thenReturn(transaction)
        whenever(transaction.addToBackStack(any())).thenReturn(transaction)
        navigationManager = NavigationManager(fragmentManager, fragmentContainerId)
    }

    @Test
    fun goTo_backstackIsEmpty_addsFragment() {
        whenever(fragmentManager.backStackEntryCount).thenReturn(0)

        navigationManager.goTo(fragment)

        verify(transaction).add(fragmentContainerId, fragment)
        verify(transaction).addToBackStack("Fragment")
        verify(transaction).commit()
    }

    @Test
    fun goTo_backstackIsNotEmpty_replacesFragment() {
        whenever(fragmentManager.backStackEntryCount).thenReturn(1)

        navigationManager.goTo(fragment)

        verify(transaction).replace(fragmentContainerId, fragment)
        verify(transaction).addToBackStack("Fragment")
        verify(transaction).commit()
    }

    @Test
    fun popTo() {
        val navigationManagerSpy = spy(navigationManager)

        navigationManagerSpy.popTo(fragment)

        verify(fragmentManager).popBackStack("Fragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        verify(navigationManagerSpy).goTo(fragment)
    }

    @Test
    fun goBack_lessThanTwoEntriesInBackstack_doesNothing() {
        whenever(fragmentManager.fragments).thenReturn(emptyList())

        navigationManager.goBack()

        verify(fragmentManager, never()).popBackStack()
    }

    @Test
    fun goBack_currentFragmentImplementsHandleOnBack_callsOnBack() {
        val currentFragment = mock<NewHouseholdFragment>()
        val mockSingle = mock<Single<Boolean>>()
        whenever(currentFragment.onBack()).thenReturn(mockSingle)
        whenever(fragmentManager.fragments).thenReturn(listOf(fragment, currentFragment))
        whenever(fragmentManager.backStackEntryCount).thenReturn(2)

        navigationManager.goBack()

        verify(fragmentManager).executePendingTransactions()
        verify(mockSingle).subscribe(any<NavigationManager.OnBackObserver>())
        verify(fragmentManager, never()).popBackStack()
    }

    @Test
    fun goBack_currentFragmentDoesNotImplementHandleOnBack_popsBackStack() {
        val currentFragment = mock<Fragment>()
        whenever(fragmentManager.fragments).thenReturn(listOf(fragment, currentFragment))
        whenever(fragmentManager.backStackEntryCount).thenReturn(2)

        navigationManager.goBack()

        verify(fragmentManager).executePendingTransactions()
        verify(fragmentManager).popBackStack()
    }

    @Test
    fun OnBackObserver_onSuccess_true_popsBackStack() {
        val onBackObserver = navigationManager.OnBackObserver()

        onBackObserver.onSuccess(true)

        verify(fragmentManager).popBackStack()
    }

    @Test
    fun OnBackObserver_onSuccess_false_doesNotPopBackStack() {
        val onBackObserver = navigationManager.OnBackObserver()

        onBackObserver.onSuccess(false)

        verify(fragmentManager, never()).popBackStack()
    }
}
