package com.example.greetingcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.greetingcard.ui.theme.GreetingCardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GreetingCardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
@Composable
fun MyApp(modifier: Modifier = Modifier, names: List<String> = listOf("World", "COmpose"))
{
    Surface(
        modifier=modifier,
        color = MaterialTheme.colorScheme.background
    ){
        Column(modifier = modifier.padding(vertical = 4.dp)){
            for(name in names)
        Greeting(name);}
}
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var expanded = remember{mutableStateOf(false)}
    var extraPadding = if(expanded.value)48.dp else 4.dp
    Surface(color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(all = 12.dp)
        ){Row(){
        Column(modifier = modifier.padding(12.dp)){
    Text(
        text = "Hello $name!",
        modifier = modifier.padding(12.dp)
    )}
        ElevatedButton(
            onClick = {expanded.value = !expanded.value}
        ){
            Text(
                if(expanded.value) "show less" else "show more"
            )
        }
        }}
}

@Preview(showBackground = true, widthDp = 160)
@Composable
fun GreetingPreview() {
    GreetingCardTheme {
        Greeting("ANdroid")
    }
}