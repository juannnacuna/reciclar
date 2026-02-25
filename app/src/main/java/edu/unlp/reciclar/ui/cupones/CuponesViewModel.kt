package edu.unlp.reciclar.ui.cupones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.unlp.reciclar.data.local.dao.CanjeDao
import edu.unlp.reciclar.data.local.dao.CuponDao
import edu.unlp.reciclar.data.local.entity.Canje
import edu.unlp.reciclar.data.local.entity.Cupon
import edu.unlp.reciclar.data.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CuponesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val canjeDao: CanjeDao,
    private val cuponDao: CuponDao
) : ViewModel() {

    private val _cuponesState = MutableStateFlow<List<Cupon>>(emptyList())
    val cuponesState: StateFlow<List<Cupon>> = _cuponesState.asStateFlow()

    private val _canjeResult = Channel<Result<String>>()
    val canjeResult = _canjeResult.receiveAsFlow()

    init {
        loadCupones()
    }

    private fun loadCupones() {
        viewModelScope.launch {
            try {
                val usuarioId = userRepository.getUser().getOrNull()?.id ?: return@launch
                cuponDao.getCuponesNoCanjeadosPorUsuario(usuarioId).collect { cupones ->
                    _cuponesState.value = cupones
                }
            } catch (e: Exception) {
                _cuponesState.value = emptyList()
                _canjeResult.send(Result.failure(Exception("Error al recuperar los cupones: ${e.message}")))
            }
        }
    }

    fun canjearCupon(cupon: Cupon) {
        viewModelScope.launch {
            val userResult = userRepository.getUser()
            val user = userResult.getOrNull()

            if (user == null) {
                _canjeResult.send(Result.failure(Exception("No se pudo obtener el usuario")))
                return@launch
            }

            if (user.puntosDisponibles < cupon.puntosNecesarios) {
                _canjeResult.send(Result.failure(Exception("No tienes puntos suficientes")))
                return@launch
            }

            try {
                val nuevoCanje = Canje(
                    usuarioId = user.id,
                    cuponId = cupon.id
                )
                canjeDao.insertCanje(nuevoCanje)

                _canjeResult.send(Result.success("¡Cupón canjeado con éxito!"))
            } catch (e: Exception) {
                _canjeResult.send(Result.failure(Exception("Error al procesar el canje: ${e.message}")))
            }
        }
    }
}
