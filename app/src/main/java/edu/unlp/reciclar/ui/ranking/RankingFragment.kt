package edu.unlp.reciclar.ui.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
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
        super.onViewCreated(view, savedInstanceState)

        setupLogoutButton(view, R.id.action_rankingFragment_to_loginFragment)

        val apiService = ApiClient.getApiService(requireContext())
        val rankingRepository = RankingRepository(apiService)
        val viewModelFactory = RankingViewModelFactory(rankingRepository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(RankingViewModel::class.java)

        setupRecyclerView(view)
        setupSpinner(view)
        observeViewModel(view)

        // La carga inicial se disparará desde el listener del spinner
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvRanking)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        rankingAdapter = RankingAdapter(emptyList())
        recyclerView.adapter = rankingAdapter
    }

    private fun setupSpinner(view: View) {
        val spinner = view.findViewById<Spinner>(R.id.spinnerResidueType)
        val residueTypes = listOf("Todos", "Plastico", "Vidrio", "Papel", "Metal")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, residueTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = if (position == 0) null else residueTypes[position]
                viewModel.fetchRanking(tipoResiduo = selectedType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    private fun observeViewModel(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val errorTextView = view.findViewById<TextView>(R.id.tvError)

        viewModel.ranking.observe(viewLifecycleOwner) { rankingAdapter.updateData(it) }
        viewModel.isLoading.observe(viewLifecycleOwner) { progressBar.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.error.observe(viewLifecycleOwner) {
            errorTextView.visibility = if (it != null) View.VISIBLE else View.GONE
            errorTextView.text = it
        }
    }
}
