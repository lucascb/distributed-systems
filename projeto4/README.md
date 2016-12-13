# Projeto 4

* Modifique o Projeto 3 para que implemente o algoritmo de Comprometimento em Duas Fases, visto em aula, quando uma operação “toca” mais de um arquivo. 
* Isto é, se a criação ou remoção de um arquivo implica na criação ou remoção de seus antecessores na árvore, então a operação deve ser atômica.
* Embora o algoritmo seja projetado para tolerar falhas, sua implementação pode assumir que estas não ocorrerão, então não há necessidade de se usar temporizadores.
* Para testar o algoritmo, todo servidor deverá consultar o usuário antes de responde COMMIT ou ABORT para alguma operação.
* A comunicação entre coordenadores e participantes deverá ser feita via thrift, com operações diferentes para fases diferentes do protocolo.
