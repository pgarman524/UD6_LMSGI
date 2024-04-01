<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <html>
        
            <body>
                <h1>Datos de Editorial</h1>
                <head> <!-- Aquí van los estilos-->
                    <title>Notas de Alumnos</title>
                    <style type="text/css">
                        body { font-family: Times New Roman;  text-align: center; font-size: 12px; } 
                        .tabla-contenidos1 { background: #EBF7FF;   margin-left: auto; margin-right: auto;  width: 70%;  }
                        .tabla-contenidos2 {  background: #EBF7FF; margin-left: 15%; margin-right: 20%;  width: 30%; margin-top: 20px;}
                        
                        
                    </style>
                </head>

                <div class="tabla-contenidos1">
                <table>
                    <tr bgcolor="#4F82A3" style="color: white"  >
                      <th>Código</th>
                      <th>Título</th>
                      <th>Cod_Autor</th>
                      <th>Editorial</th>
                      <th>Edición</th>
                      <th>ISBN</th>
                      <th>NumPag</th>
                      <th>Nacionalidad</th>
                    </tr>
                    <xsl:for-each select="Libros/libro">
                        <tr>
                                <td bgcolor="#D6EAF8">
                                    <xsl:value-of select="Cod_Libro" />
                                </td>
                                <td bgcolor="#F5CBA7">
                                    <xsl:value-of select="Titulo" />
                                </td>
                                
                                <td bgcolor="#D6EAF8">
                                    <ul>
                                        <xsl:for-each select="Autores/autor">
                                        <xsl:value-of select="Cod_Autor" />
                                            
                                        </xsl:for-each>
                                    </ul>
                                </td>
                                <td bgcolor="#F5CBA7">
                                    <xsl:value-of select="Editorial" />
                                </td>
                                <td bgcolor="#D6EAF8">
                                    <xsl:value-of select="Edicion" />
                                </td>
                                <td bgcolor="#F5CBA7">
                                    <xsl:value-of select="ISBN" />
                                </td>
                                <td bgcolor="#D6EAF8">
                                    <xsl:value-of select="NumPaginas" />
                                </td>

                                <td bgcolor="#F5CBA7">
                                    <ul>
                                        <xsl:for-each select="Autores/autor">
                                        <xsl:value-of select="nacionalidad" />
                                            
                                        </xsl:for-each>
                                    </ul>
                                </td>
                               
                            </tr>
                                           
                    </xsl:for-each>

                </table>

            </div>

            <div class="tabla-contenidos2">
                <table>
                    <tr bgcolor="#4F82A3" style="color: white">
                      <th>Cod_Autor</th>
                      <th>Nombre</th>
                      <th>Apellidos</th>
                      <th>Fecha Nacimiento</th>
                   
                    </tr>
                    <xsl:for-each select="Libros/libro">
                        <tr>
                                                                
                                <td bgcolor="#D6EAF8">
                                    <ul>
                                        <xsl:for-each select="Autores/autor">
                                        <xsl:value-of select="Cod_Autor" />
                                            
                                        </xsl:for-each>
                                    </ul>
                                </td>
                                
                                <td bgcolor="#F5CBA7">
                                    <ul>
                                        <xsl:for-each select="Autores/autor">
                                        <xsl:value-of select="Nombre" />
                                            
                                        </xsl:for-each>
                                    </ul>
                                </td>

                                <td bgcolor="#D6EAF8">
                                    <ul>
                                        <xsl:for-each select="Autores/autor">
                                        <xsl:value-of select="Apellidos" />
                                            
                                        </xsl:for-each>
                                    </ul>
                                </td>
                                
                                <td bgcolor="#F5CBA7">
                                    <ul>
                                        <xsl:for-each select="Autores/autor">
                                        <xsl:value-of select="FechaNacimiento" />
                                            
                                        </xsl:for-each>
                                    </ul>
                                </td>
                               
                            </tr>
                                           
                    </xsl:for-each>

                </table>
            </div>

            </body>
            
        </html>
    </xsl:template>
</xsl:stylesheet>