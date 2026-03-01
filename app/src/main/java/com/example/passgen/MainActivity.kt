package com.example.passgen

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.passgen.ui.theme.PassGenTheme
import java.security.SecureRandom


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PassGenTheme {
                AppNavigation()
            }
        }
    }
}
// Шрифт приложения
val google_sans_fonts = FontFamily(
    Font(R.font.google_sans_regular, FontWeight.Normal),
    Font(R.font.google_sans_bold, FontWeight.Bold),
    Font(R.font.google_sans_semibold, FontWeight.SemiBold)
)

// Функция генерации
fun PassGenerator(length: Int = 16, code: String = "1234", customSymbols: String = "!@#\$%^&*()-_=+[]{};:,.<>?"): Pair<String, Int> {

    //Все символы, которые могут использоваться
    val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val lower = "abcdefghijklmnopqrstuvwxyz"
    val digits = "0123456789"
    val symbols = customSymbols

    val random = SecureRandom()

    val activeSet = mutableListOf<String>()

    if (code.toString().contains("1")) activeSet.add(upper)
    if (code.toString().contains("2")) activeSet.add(lower)
    if (code.toString().contains("3")) activeSet.add(digits)
    if (code.toString().contains("4")) activeSet.add(symbols)

    if (activeSet.isEmpty()){
        throw IllegalArgumentException("Ошибка: не выбраны символы")
    }

    val password = StringBuilder()

    for (set in activeSet){ // for проходит по каждой строке, set каждая строка
        password.append(set[random.nextInt(set.length)]) // Генерируем минимальные символы исходя из настроек
    }

    val allChars = activeSet.joinToString("") // Преобразуем список в одну строку

    //activeSets.size - кол-во строк в списке

    repeat(length - activeSet.size) { // генерация оставшихся символов
        password.append(allChars[random.nextInt(allChars.length)])

    }

    val finalPassword = password.toString().toCharArray().apply { shuffle() }.concatToString()
    // password преобразуем в строку -> разбиваем на каждый символ -> перемешиваем -> обратно строка

    val uniqueCharsCount = allChars.toSet().size // N
    val entropy = (length * kotlin.math.log2(uniqueCharsCount.toDouble())).toInt()// E в битах

    return Pair(finalPassword, entropy)

}

@Composable
fun MainScreen(navController: NavController) {

    var expanded by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }


    var length by remember { mutableStateOf("") } // длина

    // Switch
    var upper_case by remember { mutableStateOf(true) }
    var lower_case by remember { mutableStateOf(true) }
    var numbers by remember { mutableStateOf(true) }
    var symbols by remember { mutableStateOf(true) }

    var changeSymbols by remember { mutableStateOf("!@#\$%^&*()-_=+[]{};:,.<>?") }

    // Конфигуратор
    val configurator = remember(upper_case, lower_case, numbers, symbols) {
        buildList {
            if (upper_case) add("1")
            if (lower_case) add("2")
            if (numbers) add("3")
            if (symbols) add("4")
        }
    }

    val allowedSymbols = "!@#$%^&*()-_=+[]{};:,.<>?"

    var generatedPassword by remember { mutableStateOf("") } // Cгенерированный пароль

    // Cила пароля
    var percentage by remember { mutableStateOf(0.0) }
    var shownIndicatorColor by remember { mutableStateOf(Color.Gray) }
    var infoPass by remember { mutableStateOf("") }
    var textOfPower by remember { mutableStateOf("") }

    val scrollState = rememberScrollState() // Cкролл для маленьких экранов

    // Меню
    val optionsMenu = listOf(
        "Сбросить фильтры" to R.drawable.reset_settings,
        "Советы по безопасности" to R.drawable.privacy_about,
        "О приложении" to R.drawable.info
    )

    val darkTheme = isSystemInDarkTheme()

    // Цвета
    val background = colorScheme.background
    val primary = colorScheme.primary
    val primaryContainer = colorScheme.primaryContainer
    val textColor = if (darkTheme) Color.White else Color.Black

    // Сброс настроек
    fun reset_Settings(){
        length = ""
        upper_case = true
        lower_case = true
        numbers = true
        symbols = true
        changeSymbols = "!@#\$%^&*()-_=+[]{};:,.<>?"
    }

    Column(
        modifier = Modifier
            .background(background)
            .fillMaxSize()
            .padding(top = 50.dp, start = 14.dp, end = 14.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Генератор паролей",
                fontSize = 30.sp,
                fontFamily = google_sans_fonts,
                fontWeight = SemiBold,
                modifier = Modifier.weight(1f),
                color = textColor
            )

            IconButton(
                onClick = { menu = true },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Меню",
                    tint = textColor
                )
            }

            DropdownMenu(
                expanded = menu,
                onDismissRequest = { menu = false },
                offset = DpOffset(x = 260.dp, y = 0.dp),
                shape = RoundedCornerShape(25.dp),
            ) {
                optionsMenu.forEach { (screen, icon) ->
                    DropdownMenuItem(
                        text = { Text(screen) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = screen,
                                modifier = Modifier
                                    .size(30.dp)
                            )
                        },
                        onClick = {
                            menu = false

                            when(screen) {
                                "Сбросить фильтры" -> reset_Settings()
                                "Советы по безопасности" -> navController.navigate("info_sec")
                                "О приложении" -> navController.navigate("info")
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Длина пароля
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Длина пароля:",
                fontSize = 23.sp,
                fontFamily = google_sans_fonts,
                color = textColor
            )

            Spacer(modifier = Modifier.width(5.dp))

            OutlinedTextField(
                value = length,
                onValueChange = { newValue ->
                    val onlyDigits = newValue.filter { it.isDigit() }
                    if (onlyDigits.length <= 2) { // Ограничение до 60 (2 знака)
                        val num = onlyDigits.toIntOrNull() ?: 0
                        if (num <= 60) length = onlyDigits
                    }
                },
                label = { Text(text = " 8 — 60", fontSize = 16.sp, fontFamily = google_sans_fonts, textAlign = TextAlign.Center, fontWeight = SemiBold, color = primary ) },
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontFamily = google_sans_fonts,
                    color = textColor

                ),
                modifier = Modifier
                    .weight(1f)
                    .height(70.dp),
                shape = RoundedCornerShape(25.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, // Фон поля при фокусе
                    unfocusedContainerColor = Color.Transparent, // Фон поля без фокуса
                    disabledContainerColor = Color.Transparent, // Фон поля в отключенном состоянии

                    focusedIndicatorColor = primary,
                    unfocusedIndicatorColor = primary,
                    disabledIndicatorColor = primary,

                    focusedTextColor = textColor, // Цвет текста при фокусе
                    unfocusedTextColor = textColor, // Цвет текста без фокуса
                    disabledTextColor = textColor, // Цвет текста в отключенном состоянии

                    cursorColor = primary, // Цвет курсора
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(5.dp))

            // Кнопка +
            Button(
                onClick = {
                    val current = length.toIntOrNull() ?: 8
                    if (current < 60) length = (current + 1).toString()
                },
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary
                )
            ) {
                Text("+", fontSize = 25.sp, color = background, fontFamily = google_sans_fonts, fontWeight = SemiBold)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Кнопка минус
            Button(
                onClick = {
                    val current = length.toIntOrNull() ?: 8
                    if (current > 8) length = (current - 1).toString()
                },
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary
                )
            ) {
                Text("—", fontSize = 25.sp, color = background, fontFamily = google_sans_fonts, fontWeight = SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        // Фмльтры для пароля

        // Upper_case
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                painter = painterResource(id = R.drawable.upper),
                contentDescription = "Upper_case",
                tint = primary,
                modifier = Modifier
                    .size(40.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Заглавные буквы",
                fontSize = 25.sp,
                fontFamily = google_sans_fonts,
                modifier = Modifier.weight(1f),
                color = textColor
            )
            Switch(
                checked = upper_case,
                onCheckedChange = { checked ->
                    if (!checked && configurator.size == 1) return@Switch
                    upper_case = checked
                }
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Lower_case
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                painter = painterResource(id = R.drawable.lower),
                contentDescription = "Lower_case",
                tint = primary,
                modifier = Modifier
                    .size(40.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Строчные буквы",
                fontSize = 25.sp,
                fontFamily = google_sans_fonts,
                modifier = Modifier.weight(1f),
                color = textColor
            )
            Switch(
                checked = lower_case,
                onCheckedChange = { checked ->
                    if (!checked && configurator.size == 1) return@Switch
                    lower_case = checked
                }
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Numbers
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                painter = painterResource(id = R.drawable.numbers),
                contentDescription = "Numbers",
                tint = primary,
                modifier = Modifier
                    .size(40.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Цифры",
                fontSize = 25.sp,
                fontFamily = google_sans_fonts,
                modifier = Modifier.weight(1f),
                color = textColor
            )
            Switch(
                checked = numbers,
                onCheckedChange = { checked ->
                    if (!checked && configurator.size == 1) return@Switch
                    numbers = checked
                }
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Symbols
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                painter = painterResource(id = R.drawable.symbols),
                contentDescription = "Symbols",
                tint = primary,
                modifier = Modifier
                    .size(40.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Символы",
                fontSize = 25.sp,
                fontFamily = google_sans_fonts,
                color = textColor
            )

            val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f) // анимация

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = textColor,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation)
                    .clickable { expanded = !expanded }
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = symbols,
                onCheckedChange = { checked ->
                    if (!checked && configurator.size == 1) return@Switch
                    symbols = checked
                }
            )
        }
        // Меню настройки символов
        if(expanded){
            OutlinedTextField(
                value = changeSymbols,
                onValueChange = { newValue ->
                    changeSymbols = newValue.filter { it in allowedSymbols }.toList().distinct().joinToString("") // убираем дубликацию, если пользователь ввёл одинаковые символы
                },
                modifier = Modifier
                    .padding(start = 54.dp)
                    .width(250.dp)
                    .height(60.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontFamily = google_sans_fonts,

                    ),
                shape = RoundedCornerShape(25.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, // Фон поля при фокусе
                    unfocusedContainerColor = Color.Transparent, // Фон поля без фокуса
                    disabledContainerColor = Color.Transparent, // Фон поля в отключенном состоянии

                    focusedIndicatorColor = primary,
                    unfocusedIndicatorColor = primary,
                    disabledIndicatorColor = primary,

                    focusedTextColor = textColor, // Цвет текста при фокусе
                    unfocusedTextColor = textColor, // Цвет текста без фокуса
                    disabledTextColor = textColor, // Цвет текста в отключенном состоянии

                    cursorColor = primary, // Цвет курсора
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        val codeStr = configurator.joinToString("")

        // Обращение к функции генерации
        val (generate, strength) = PassGenerator(
            length = length.toIntOrNull() ?: 8,
            code = codeStr,
            customSymbols = changeSymbols
        )

        val lengthInt = length.toIntOrNull() ?: 8

        // Кнопка генерации
        Button(
            onClick = {
                generatedPassword = generate
                percentage = ((strength.toFloat() / 100) - 0.2)
                infoPass = ": $strength бит"

                textOfPower = when (strength) {
                    in 0..28 -> "Очень слабый"
                    in 29..37 -> "Слабый"
                    in 38..70 -> "Средний"
                    in 71..120 -> "Сильный"
                    else -> "Очень сильный"
                }

                shownIndicatorColor = when (textOfPower){
                    "Очень слабый" -> Color(red = 103, green = 80, blue = 164)
                    "Слабый" ->  Color(red = 255, green = 56, blue = 60)
                    "Средний" -> Color(red = 255, green = 204, blue = 0)
                    "Сильный" -> Color(red = 52, green = 199, blue = 89)
                    else -> Color(red = 25, green = 55, blue = 255)
                }
            },
            enabled = lengthInt >= 8, // активна если длина >=8
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.generate),
                contentDescription = "Generate",
                tint = background,
                modifier = Modifier
                    .size(34.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Сгенерировать",
                fontSize = 26.sp,
                fontFamily = google_sans_fonts,
                fontWeight = SemiBold,
                color = background
            )
        }


        Spacer(modifier = Modifier.height(30.dp))


        // Окно с паролем
        if(generatedPassword.isNotEmpty()) {


            val rowHeight = if (generate.length >= 40) 100.dp else 60.dp

            val clipboardManager = LocalClipboardManager.current
            val context = LocalContext.current

            Row(
                modifier = Modifier
                    .background(color = primaryContainer, shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth()
                    .height(rowHeight)
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = generatedPassword,
                    fontSize = 20.sp,
                    fontFamily = google_sans_fonts,
                    fontWeight = SemiBold,
                    modifier = Modifier.weight(1f).padding(end = 20.dp),
                    color = textColor
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(generate))
                        Toast.makeText(context, "Пароль скопирован в буфер обмена", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .width(60.dp)
                        .height(rowHeight)
                        .background(
                            color = primary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.content_copy),
                        contentDescription = "copy",
                        tint = background,
                        modifier = Modifier
                            .size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            // Сила пароля
            Row(
                modifier = Modifier
                    .background(color = Color(red = 231, green = 231, blue = 231, alpha = 255), shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth()
                    .height(10.dp)
            ){
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentage.toFloat())
                        .background(color = shownIndicatorColor, shape = RoundedCornerShape(16.dp))
                        .height(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "$textOfPower ${infoPass}",
                    fontSize = 20.sp,
                    fontFamily = google_sans_fonts,
                    color = textColor
                )
            }
        }
    }
}

// Экран "О приложении"
@Composable
fun InfoScreen(navController: NavController){

    var menu by remember { mutableStateOf(false) }

    val optionsMenu = listOf(
        "Генерация" to R.drawable.generate,
        "Советы по безопасности" to R.drawable.privacy_about,
    )

    val darkTheme = isSystemInDarkTheme()

    // Цвета
    val background = colorScheme.background
    val primary = colorScheme.primary
    val primaryContainer = colorScheme.primaryContainer
    val textColor = if (darkTheme) Color.White else Color.Black

    val scrollState = rememberScrollState() // скролл для маленьких экранов

    Column(
        modifier = Modifier
            .background(background)
            .fillMaxSize()
            .padding(top = 50.dp, start = 14.dp, end = 14.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Генератор паролей",
                fontSize = 30.sp,
                fontFamily = google_sans_fonts,
                fontWeight = SemiBold,
                modifier = Modifier.weight(1f),
                color = textColor
            )

            IconButton(
                onClick = { menu = true },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Меню",
                    tint = textColor
                )
            }

            DropdownMenu(
                expanded = menu,
                onDismissRequest = { menu = false },
                offset = DpOffset(x = 260.dp, y = 0.dp),
                shape = RoundedCornerShape(25.dp),
            ) {
                optionsMenu.forEach { (screen, icon) ->
                    DropdownMenuItem(
                        text = { Text(screen) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = screen,
                                modifier = Modifier
                                    .size(30.dp)
                            )
                        },
                        onClick = {
                            menu = false

                            when (screen) {
                                "Генерация" -> navController.navigate("main")
                                "Советы по безопасности" -> navController.navigate("info_sec")
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "О приложении",
            fontSize = 24.sp,
            fontFamily = google_sans_fonts,
            fontWeight = SemiBold,
            color = primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Описание
        Text(
            text = "Это приложение помогает создавать надёжные пароли и оценивать их стойкость, чтобы ваши данные оставались в безопасности.",
            fontSize = 18.sp,
            fontFamily = google_sans_fonts,
            color = textColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Блок 1
        Column {
            Text(
                text = "Назначение",
                fontSize = 22.sp,
                fontFamily = google_sans_fonts,
                color = primary,
                fontWeight = SemiBold,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Приложение предназначено для генерации сложных паролей и помощи в выборе надёжных комбинаций.",
                fontSize = 18.sp,
                fontFamily = google_sans_fonts,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Блок 2
        Column {
            Text(
                text = "Безопасность",
                fontSize = 22.sp,
                fontFamily = google_sans_fonts,
                color = primary,
                fontWeight = SemiBold
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Все пароли генерируются локально на вашем устройстве и не передаются в сеть.",
                fontSize = 18.sp,
                fontFamily = google_sans_fonts,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Блок 3
        Column {
            Text(
                text = "Для кого",
                fontSize = 22.sp,
                fontFamily = google_sans_fonts,
                color = primary,
                fontWeight = SemiBold
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Подходит для всех, кто заботится о безопасности своих аккаунтов и личных данных.",
                fontSize = 18.sp,
                fontFamily = google_sans_fonts,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Акцентная карточка
        Surface(
            color = primaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = textColor)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Используйте уникальные пароли для каждого сервиса и регулярно обновляйте их.",
                    fontSize = 18.sp,
                    fontFamily = google_sans_fonts,
                    color = textColor
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = "По вопросам и предложениям:\n@motchug в Telegram",
                fontSize = 18.sp,
                fontFamily = google_sans_fonts,
                textAlign = TextAlign.Center,
                color = textColor
            )
        }
    }
}

// Экран "Советы по защите пароля"
@Composable
fun InfoSecScreen(navController: NavController){

    var menu by remember { mutableStateOf(false) }

    val optionsMenu = listOf(
        "Генерация" to R.drawable.generate,
        "О приложении" to R.drawable.info,
    )

    val darkTheme = isSystemInDarkTheme()

    // Цвета
    val background = colorScheme.background
    val primary = colorScheme.primary
    val primaryContainer = colorScheme.primaryContainer
    val textColor = if (darkTheme) Color.White else Color.Black

    val scrollState = rememberScrollState() // скролл для маленьких экранов

    Column(
        modifier = Modifier
            .background(background)
            .fillMaxSize()
            .padding(top = 50.dp, start = 14.dp, end = 14.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Генератор паролей",
                fontSize = 30.sp,
                fontFamily = google_sans_fonts,
                fontWeight = SemiBold,
                modifier = Modifier.weight(1f),
                color = textColor
            )

            IconButton(
                onClick = { menu = true },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Меню",
                    tint = textColor
                )
            }

            DropdownMenu(
                expanded = menu,
                onDismissRequest = { menu = false },
                offset = DpOffset(x = 260.dp, y = 0.dp),
                shape = RoundedCornerShape(25.dp),
            ) {
                optionsMenu.forEach { (screen, icon) ->
                    DropdownMenuItem(
                        text = { Text(screen) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = screen,
                                modifier = Modifier
                                    .size(30.dp)
                            )
                        },
                        onClick = {
                            menu = false

                            when(screen) {
                                "Генерация" -> navController.navigate("main")
                                "О приложении" -> navController.navigate("info")
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Советы по защите пароля",
            fontSize = 24.sp,
            fontFamily = google_sans_fonts,
            fontWeight = SemiBold,
            color = primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Блок 1
        Column {
            Text(text = "1. Длина пароля",
                fontSize = 22.sp,
                fontFamily = google_sans_fonts,
                color = primary,
                fontWeight = SemiBold
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Используйте не менее 12 символов. Чем длиннее пароль, тем он устойчивее к взлому.",
                fontSize = 18.sp,
                fontFamily = google_sans_fonts,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Блок 2
        Column {
            Text(text = "2. Сложность",
                fontSize = 22.sp,
                fontFamily = google_sans_fonts,
                color = primary,
                fontWeight = SemiBold
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Добавляйте цифры, заглавные, строчные буквы и специальные знаки (например, !, @, #).",
                fontSize = 18.sp,
                fontFamily = google_sans_fonts,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Блок 3
        Column {
            Text(text = "3. Уникальность",
                fontSize = 22.sp,
                fontFamily = google_sans_fonts,
                color = primary,
                fontWeight = SemiBold
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Не используйте один и тот же пароль для почты, банков и соцсетей.",
                fontSize = 18.sp,
                fontFamily = google_sans_fonts,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column {
            Text(text = "4. Личные данные",
                fontSize = 22.sp,
                fontFamily = google_sans_fonts,
                color = primary,
                fontWeight = SemiBold
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Не используйте в паролях имена, даты рождения, номера телефонов и другие личные данные — их легко угадать.",
                fontSize = 18.sp,
                fontFamily = google_sans_fonts,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Акцентная карточка внизу
        Surface(
            color = primaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = textColor)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Используйте генератор для создания надёжных паролей и менеджер паролей, чтобы безопасно их хранить.",
                    fontSize = 18.sp,
                    fontFamily = google_sans_fonts,
                    color = textColor
                )
            }
        }
    }
}

// Навигация между экранами
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("info") { InfoScreen(navController) }
        composable("info_sec") {InfoSecScreen(navController)}

    }
}

@Preview(device = "id:pixel_9", showSystemUi = true)
@Composable
fun GreetingPreview() {
    PassGenTheme {
        AppNavigation()
    }
}