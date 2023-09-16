# Модуль терминала

![Боюсь, это терминал](item:tis3d:terminal_module)

Модуль терминала обеспечивает более расширенную форму ввода, чем [модуль клавиатуры](keypad_module.md), и более конкретизированную форму вывода, чем [модуль дисплея](display_module.md).

Пользователь может вводить одну строку текста, которая при отправке записывается символ за символом во все порты терминального модуля. Символы преобразуются в числовые значения в соответствии с CP437. Дальнейшие вводимые данные игнорируются до тех пор, пока не будет передана полная строка. На это указывает, что строка ввода неактивна, а курсор не мигает. Тем не менее, можно ввести больше текста в этом состоянии, чтобы подготовиться к тому моменту, когда отправка снова будет включена.

Значения постоянно считываются со всех четырёх портов модуля терминала. Полученные значения усекаются до 8-битных значений (маскируется 0xFF) и интерпретируются как символы из CP437, за исключением нескольких кодов, которые интерпретируются как управляющие символы. К этим управляющим символам относятся `\a` (звуковой сигнал), `\b` (возврат на шаг), `\t` (горизонтальная табуляция), а также `\n` и `\r` (оба приводят к переводу строки).

Если индекс записи в текущей строке достиг максимальной длины строки и было считано значение, автоматически будет создана новая строка (автоматический перенос строки). Если в последней строке текста, которая может быть отображена, появляется новая строка, все строки будут прокручиваться на одну, отбрасывая первую строку.

Значение из обрабатываемой в настоящее время входной строки может быть передано только на один порт, т. е. значения никогда не будут дублироваться; даже если в одном цикле [контроллера](../block/controller.md) произойдёт многократное чтение, будет выполнено только одно.