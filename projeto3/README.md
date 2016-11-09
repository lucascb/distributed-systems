# Projeto 3

* Modifique o seu segundo projeto para que aceite conexões Thrift.

* Várias instâncias do servidor devem ser iniciadas (número não fixo), e todas devem conhecer todas as outras.

* O cliente se conecta a um servidor qualquer.

* Deverão ser suportadas as seguintes operações:
** GET - Retorna dados e metadados do arquivo.
** LIST - Retorna lista de filhos do arquivo.
** ADD - Adiciona arquivo
** UPDATE - Atualiza arquivos
** DELETE - Apaga arquivo
** UPDATE+VERSION - Atualiza arquivos se versão atual for mesma especificada.
** DELETE+VERSION - Apaga arquivo se a versão atual for a mesma especificada.

* A requisição do cliente é encaminhada pelo servidor em que está conectado para o servidor que deve lidar com a requisição

* Algum mecanismo de particionamento dos dados deve ser implementado pelos servidores.
