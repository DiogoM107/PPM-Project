package main.scala.ppm

import java.nio.file.{Files, Paths}
import scala.io.StdIn.readLine

object TextUI {

  def getFileName(): String = {
    println("Indique o nome do ficheiro: ")
    val file = readLine.trim
    if (Files.exists(Paths.get(file))) {
      file
    }
    else {
      println("Ficheiro não encontrado!")
      getFileName()
    }
  }

  def getOption(): Int = {
    val maxOpt = getMenu()
    val opt = readLine.trim
    opt.toIntOption match {
      case None => {
        println("Tem de inserir uma opção válida!")
        getOption()
      }
      case Some(x) => {
        if (x > 0 && x <= maxOpt) {
          x
        }
        else {
          println("Tem de inserir uma opção válida!")
          getOption()
        }
      }
    }

  }

  //Retorna a opcao maior do menu
  def getMenu(): Int = {
    println("Indique a operação a realizar à octree (Insira o número correspondente):")
    println("1 - Escalar a octree (2x).")
    println("2 - Escalar a octree (3x).")
    println("3 - Aplicar efeito de cor (sépia).")
    println("4 - Aplicar efeito de cor (remover verdes).")
    println("5 - Sair")
    //Opcao maxima do menu
    5
  }

}
