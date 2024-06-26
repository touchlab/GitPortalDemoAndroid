package co.touchlab.kampkit.android.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kampkit.android.R
import co.touchlab.kampkit.android.models.BreedViewModel
import co.touchlab.gitportaltemplate.db.Breed
import co.touchlab.gitportaltemplate.repository.BreedDataEvent
import co.touchlab.gitportaltemplate.repository.BreedDataRefreshState
import co.touchlab.gitportaltemplate.repository.BreedDataState
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: BreedViewModel,
) {
    val dataState by viewModel.dataState.collectAsStateWithLifecycle()
    val dataEvent by viewModel.dataEventState.collectAsStateWithLifecycle()
    val breedList by viewModel.breedListState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    MainScreenContent(
        dataState = dataState,
        dataEvent = dataEvent,
        breedList = breedList,
        onRefresh = { scope.launch { viewModel.refreshBreeds() } },
        onFavorite = { scope.launch { viewModel.updateBreedFavorite(it) } }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreenContent(
    dataState: BreedDataRefreshState,
    dataEvent: BreedDataEvent,
    breedList: List<Breed>,
    onRefresh: () -> Unit = {},
    onFavorite: (Breed) -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        val refreshState = rememberPullRefreshState(dataEvent is BreedDataEvent.Loading, onRefresh)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (dataEvent) {
                is BreedDataEvent.Error -> Text(dataEvent.reason.name, color = Color.Red)
                BreedDataEvent.Initial -> Text("")
                BreedDataEvent.Loading -> Text("Loading...")
                BreedDataEvent.RefreshedSuccess -> Text("Success")
            }

            when (dataState) {
                is BreedDataState.Cached -> Button(onRefresh) { Text("Refresh") }
                BreedDataState.Empty -> Button(onRefresh) { Text("Load Data") }
                BreedDataEvent.Loading -> Button(onRefresh, enabled = false) { Text("Refresh") }
            }

            Box(Modifier.pullRefresh(refreshState)) {
                when (dataState) {
                    is BreedDataState.Cached, BreedDataEvent.Loading -> Success(
                        successData = breedList,
                        favoriteBreed = onFavorite
                    )
                    BreedDataState.Empty -> Empty()
                }
                PullRefreshIndicator(
                    dataEvent is BreedDataEvent.Loading,
                    refreshState,
                    Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun Empty() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.empty_breeds))
    }
}

@Composable
fun Error(error: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = error)
    }
}

@Composable
fun Success(
    successData: List<Breed>,
    favoriteBreed: (Breed) -> Unit
) {
    DogList(breeds = successData, favoriteBreed)
}

@Composable
fun DogList(breeds: List<Breed>, onItemClick: (Breed) -> Unit) {
    LazyColumn {
        items(breeds) { breed ->
            DogRow(breed) {
                onItemClick(it)
            }
            Divider()
        }
    }
}

@Composable
fun DogRow(breed: Breed, onClick: (Breed) -> Unit) {
    Row(
        Modifier
            .clickable { onClick(breed) }
            .padding(10.dp)
    ) {
        Text(breed.name, Modifier.weight(1F))
        FavoriteIcon(breed)
    }
}

@Composable
fun FavoriteIcon(breed: Breed) {
    Crossfade(
        targetState = !breed.favorite,
        animationSpec = TweenSpec(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ), label = "favFade"
    ) { fav ->
        if (fav) {
            Image(
                painter = painterResource(id = R.drawable.ic_favorite_border_24px),
                contentDescription = stringResource(R.string.favorite_breed, breed.name)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_favorite_24px),
                contentDescription = stringResource(R.string.unfavorite_breed, breed.name)
            )
        }
    }
}

@Preview
@Composable
fun MainScreenContentPreview_Success() {
    MainScreenContent(
        dataState = BreedDataState.Empty,
        dataEvent = BreedDataEvent.RefreshedSuccess,
        breedList = listOf(
            Breed(0, "appenzeller", false),
            Breed(1, "australian", true)
        )
    )
}
