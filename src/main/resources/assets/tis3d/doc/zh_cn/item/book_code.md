# 编码书

![99个小BUG隐藏在代码里...](item:tis3d:book_code)

【注意】
此文档已过期，因而可能不包含英文文档中更新的内容（或其翻译）。请查阅英文版以获取更多信息。

这本编码书(或者它有时被称为**代码圣经**)是TIS-3D程序 员工作的最好的朋友。相比于简陋的书与笔，使用它可以更舒适地为代码打草稿，同时支持改正，从而更容易发现代码中的潜在错误。

要获取编码书，只需手持一本普通的书右键点击安装于[外壳](..block/casing.md)的[执行模块](module_execution.md)上即可。若要在[执行模块](module_execution.md)上安装程序，首先需在编码书中选中，并对着执行模块上使用编码书。若要从[执行模块](module_execution.md)复制现有的程序到代码书中，只需潜行对着[执行模块](module_execution.md)使用编码书即可。

By default, each page in the book will contain its own, separate program, and when programming an [execution module](module_execution.md) only that page's content will be programmed onto it. However, if the last line of text on a page is the preprocessor macro `#BWTM`, the program will continue on to the next page, enabling you to write programs across multiple pages. When programming an [execution module](module_execution.md) while on any page of a multi-page program, the whole program will be installed on the module.