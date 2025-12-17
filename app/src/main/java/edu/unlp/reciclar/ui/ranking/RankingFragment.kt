package edu.unlp.reciclar.ui.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.unlp.reciclar.R
import edu.unlp.reciclar.data.repository.RankingRepository
import edu.unlp.reciclar.data.source.ApiClient
import edu.unlp.reciclar.ui.BaseFragment

class RankingFragment : BaseFragment() {

    private lateinit var viewModel: RankingViewModel
    private lateinit var rankingAdapter: RankingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Importante llamar al super

        // 1. Configurar el botón de logout usando la lógica del BaseFragment
        setupLogoutButton(view, R.id.action_rankingFragment_to_loginFragment)

        // 2. Inyección de dependencias para el ViewModel (la lógica de auth ya no es necesaria aquí)
        val apiService = ApiClient.getApiService(requireContext())
        val rankingRepository = RankingRepository(apiService)
        val viewModelFactory = RankingViewModelFactory(rankingRepository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(RankingViewModel::class.java)

        // 3. Configurar RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvRanking)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        rankingAdapter = RankingAdapter(emptyList())
        recyclerView.adapter = rankingAdapter

        // 4. Observar LiveData
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val errorTextView = view.findViewById<TextView>(R.id.tvError)

        viewModel.ranking.observe(viewLifecycleOwner) { rankingAdapter.updateData(it) }
        viewModel.isLoading.observe(viewLifecycleOwner) { progressBar.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.error.observe(viewLifecycleOwner) {
            errorTextView.visibility = if (it != null) View.VISIBLE else View.GONE
            errorTextView.text = it
        }

        // 5. Cargar datos
        viewModel.fetchRanking()
    }
}