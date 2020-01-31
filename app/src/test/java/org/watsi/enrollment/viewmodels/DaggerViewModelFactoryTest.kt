package org.watsi.enrollment.viewmodels

import android.arch.lifecycle.ViewModel
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import javax.inject.Provider

@RunWith(MockitoJUnitRunner::class)
class DaggerViewModelFactoryTest {

    @Mock lateinit var mockProvider: Provider<ViewModel>
    val viewModelProviderMap = mutableMapOf<Class<out ViewModel>, Provider<ViewModel>>()
    lateinit var factory: DaggerViewModelFactory

    @Before
    fun setup() {
        factory = DaggerViewModelFactory(viewModelProviderMap)
    }

    @Test(expected = DaggerViewModelFactory.UnsupportedViewModelException::class)
    fun viewModelClassDoesNotExistInMap_throwsException() {
        factory.create(FooViewModel::class.java)
    }

    @Test
    fun viewModelClassExistsInMap_returnsViewModel() {
        val viewModelClass = FooViewModel::class.java
        val viewModel = FooViewModel()

        whenever(mockProvider.get()).thenReturn(viewModel)

        viewModelProviderMap[viewModelClass] = mockProvider

        assertEquals(viewModel, factory.create(viewModelClass))
    }

    @Test
    fun subclassOfViewModelExistsInMap_returnsViewModel() {
        val viewModel = FooViewModel()

        whenever(mockProvider.get()).thenReturn(viewModel)

        viewModelProviderMap[BarViewModel::class.java] = mockProvider

        assertEquals(viewModel, factory.create(FooViewModel::class.java))
    }

    open class FooViewModel : ViewModel()
    class BarViewModel : FooViewModel()
}
