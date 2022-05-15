# PPM-Project

Projeto realizado na cadeira de PPM (Projeto Programação Multi-Paradigma) no ISCTE.

Para escolher entre executar o projeto com a interface textual ou gráfica é necessário ir à função start da classe Main.scala e descomentar a chamada à interface que pretendemos executar e comentar a outra.

O ficheiro a carregar deve ser o config.txt. Depois de executar uma vez e fazer operações à Octree, os objetos alterados são gravados no ficheiro output.txt. Ao carregar o ficheiro output.txt, serão carregados os objetos e a octree que foi criada na execução anterior.

Ao fazer download do projeto, se o for editar então deve import o CodeStyles.xml (IDE usado foi o InteliJ).

Para o correcto funcionamente do programa, deve seguir a formatação do exemplo abaixo:

Cube (50,50,255) 24 24 24 8.0 8.0 8.0

É possível criar um Cube ou um Cylinder.
Os primeiros 3 números entre parênteses correspondem aos valores do RGB.
Os 3 números seguintes correspondem aos valores de translação.
Os últimos 3 números correspondem aos valores da escala.