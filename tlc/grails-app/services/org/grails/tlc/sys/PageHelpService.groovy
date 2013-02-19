/*
 *  Copyright 2010-2013 Paul Fernley.
 *
 *  This file is part of the Three Ledger Core (TLC) software
 *  from Paul Fernley.
 *
 *  TLC is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TLC is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TLC. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grails.tlc.sys

class PageHelpService {

    static transactional = false

    def hasPageHelp(code, locale, cacheService) {
        def loc = cacheService.get('pageHelp', 0L, code + CacheService.IMPOSSIBLE_VALUE + locale.language + locale.country)
        if (loc == null) {
            def sub
            def texts = SystemPageHelp.findAllByCodeAndLocaleInList(code, ['*', locale.language, locale.language + locale.country], [sort: 'relevance', oder: 'desc', max: 1])
            if (!texts && code.indexOf('.') != -1) {
                sub = code.substring(0, code.lastIndexOf('.'))
                while (true) {
                    texts = SystemPageHelp.findAllByCodeAndLocaleInList(sub, ['*', locale.language, locale.language + locale.country], [sort: 'relevance', oder: 'desc', max: 1])
                    if (texts) break
                    if (sub.indexOf('.') == -1) {
                        sub = null
                        break
                    }

                    sub = sub.substring(0, sub.lastIndexOf('.'))
                }
            }

            if (texts) {
                loc = texts[0].locale
                if (sub) loc = loc + CacheService.IMPOSSIBLE_VALUE + sub
            } else {
                loc = CacheService.IMPOSSIBLE_VALUE
            }

            cacheService.put('pageHelp', 0L, code + CacheService.IMPOSSIBLE_VALUE + locale.language + locale.country, loc)
        }

        return (loc == CacheService.IMPOSSIBLE_VALUE) ? null : loc
    }

    def getPageHelp(code, locale, cacheService, applicationURI) {
        def loc = hasPageHelp(code, locale, cacheService)
        if (!loc) return null
        def pos = loc.indexOf(CacheService.IMPOSSIBLE_VALUE)
        if (pos != -1) {
            code = loc.substring(pos + 1)
            loc = loc.substring(0, pos)
        }

        def help = SystemPageHelp.findByCodeAndLocale(code, loc)
        return markup(help?.text, applicationURI)
    }

    private markup(source, applicationURI) {
        if (!source) return ''
        def lines = source.split('\n')*.replaceAll('\\s+$', '')
        source = null   // Clean up a little
        def blocks = []
        def divActive = false
        for (line in lines) {
            switch (getLineType(line, divActive)) {
                case 'p':
                    appendText(blocks, line)
                    break
                case 'hr':
                    blocks << new MarkupBlock('hr')
                    break
                case 'h':
                    def pos = line.indexOf(' ')
                    blocks << new MarkupBlock("h${Math.min(pos - 1, 3)}", line.substring(pos).trim())
                    break
                case 'tr':
                    appendTableRow(blocks, line)
                    break
                case 'ul':
                    appendUnorderedListItem(blocks, line)
                    break
                case 'ol':
                    appendOrderedListItem(blocks, line)
                    break
                case '+d':
                    blocks << new MarkupBlock('+d')
                    divActive = true
                    break
                case '-d':
                    blocks << new MarkupBlock('-d')
                    divActive = false
                    break
                default:
                    blocks << new MarkupBlock('')
                    break
            }
        }

        // Close any helpbox div
        if (divActive) blocks << new MarkupBlock('-d')

        lines = []
        for (block in blocks) {
            createLines(lines, block, applicationURI)
        }

        return lines
    }

    private getLineType(line, divActive) {
        if (line ==~ /\* +\S.*/) return 'ul'
        if (line ==~ /# +\S.*/) return 'ol'
        if (line ==~ /==+ +\S.*/) return 'h'
        if (line ==~ /\|.*\|/) return 'tr'
        if (line == '----') return 'hr'
        if (line == '[[' && !divActive) return '+d'
        if (line == ']]' && divActive) return '-d'
        if (line) return 'p'
        return ''
    }

    private appendText(blocks, line) {
        if (blocks.size() == 0 || blocks.last().type != 'p') {
            blocks << new MarkupBlock('p', line)
        } else {
            blocks.last().lines << line
        }
    }

    private appendTableRow(blocks, line) {
        if (blocks.size() == 0 || blocks.last().type != 'table') {
            blocks << new MarkupBlock('table', line)
        } else {
            blocks.last().lines << line
        }
    }

    private appendUnorderedListItem(blocks, line) {
        if (blocks.size() == 0 || blocks.last().type != 'ul') {
            blocks << new MarkupBlock('ul', line.substring(line.indexOf(' ')).trim())
        } else {
            blocks.last().lines << line.substring(line.indexOf(' ')).trim()
        }
    }

    private appendOrderedListItem(blocks, line) {
        if (blocks.size() == 0 || blocks.last().type != 'ol') {
            blocks << new MarkupBlock('ol', line.substring(line.indexOf(' ')).trim())
        } else {
            blocks.last().lines << line.substring(line.indexOf(' ')).trim()
        }
    }

    private createLines(lines, block, applicationURI) {
        switch (block.type) {
            case 'p':
                def line = ''
                for (it in block.lines) {
                    if (line) line += '\n'
                    line += it
                }
				
                line = "<p>${formatLine(line, applicationURI)}</p>"
                lines.addAll(line.split('\n')*.replaceAll('\\s+$', ''))
                break
				
            case 'hr':
                lines << '<hr/>'
                break
				
            case 'h1':
                lines << "<h1>${formatLine(block.lines[0], applicationURI)}</h1>"
                break
				
            case 'h2':
                lines << "<h2>${formatLine(block.lines[0], applicationURI)}</h2>"
                break
				
            case 'h3':
                lines << "<h3>${formatLine(block.lines[0], applicationURI)}</h3>"
                break
				
            case 'table':
                lines << '<div class="center" style="margin:10px;"><table>'
                for (row in block.lines) {
                    lines << '<tr>'
                    def cols = row.split('\\|')
                    for (int i = 1; i < cols.size(); i++) {
                        lines << "<td>${formatLine(cols[i], applicationURI)}</td>"
                    }
					
                    lines << '</tr>'
                }
				
                lines << '</table></div>'
                break
				
            case 'ul':
                lines << '<ul style="list-style-type:disc;list-style-position:inside;">'
                for (it in block.lines) lines << "<li>${formatLine(it, applicationURI)}</li>"
                lines << '</ul>'
                break
				
            case 'ol':
                lines << '<ol style="list-style-type:decimal;list-style-position:inside;">'
                for (it in block.lines) lines << "<li>${formatLine(it, applicationURI)}</li>"
                lines << '</ol>'
                break
				
            case '+d':
                lines << '<div class="helpbox">'
                break
				
            case '-d':
                lines << '</div>'
                break
        }
    }

    private formatLine(line, applicationURI) {
        line = line.encodeAsHTML()
        def stack = [new MarkupBlock('')]
        StringBuilder sb = new StringBuilder()
        def nextIsLiteral = false
        for (int i = 0; i < line.length(); i++) {
            if (nextIsLiteral) {
                nextIsLiteral = false
                if (line[i] == '\n') sb.append('<br/>') // At the end of a line, \ means a line break
                sb.append(line[i])
            } else {
                switch (line[i]) {
                    case '\\':
                        nextIsLiteral = true
                        break
                    case '*':
                        if (peek(line, i) == '*') {
                            if (stack.last() == 'b') {
                                sb.append('</b>')
                                pop(stack)
                                i++
                            } else if (!stack.contains('b')) {
                                sb.append('<b>')
                                push(stack, 'b')
                                i++
                            } else {
                                sb.append(line[i])
                            }
                        } else {
                            sb.append(line[i])
                        }
                        break
                    case '/':
                        if (peek(line, i) == '/') {
                            if (stack.last() == 'i') {
                                sb.append('</i>')
                                pop(stack)
                                i++
                            } else if (!stack.contains('i')) {
                                sb.append('<i>')
                                push(stack, 'i')
                                i++
                            } else {
                                sb.append(line[i])
                            }
                        } else {
                            sb.append(line[i])
                        }
                        break
                    case '_':
                        if (peek(line, i) == '_') {
                            if (stack.last() == 'u') {
                                sb.append('</u>')
                                pop(stack)
                                i++
                            } else if (!stack.contains('u')) {
                                sb.append('<u>')
                                push(stack, 'u')
                                i++
                            } else {
                                sb.append(line[i])
                            }
                        } else {
                            sb.append(line[i])
                        }
                        break
                    case '-':
                        if (peek(line, i) == '-') {
                            if (stack.last() == 's') {
                                sb.append('</s>')
                                pop(stack)
                                i++
                            } else if (!stack.contains('s')) {
                                sb.append('<s>')
                                push(stack, 's')
                                i++
                            } else {
                                sb.append(line[i])
                            }
                        } else {
                            sb.append(line[i])
                        }
                        break
                    case '{':
	                    if (peek(line, i) == '{') {
	                        def e = line.indexOf('}}', i + 1)
	                        if (e > i) {
								def dflt
	                            def img = line.substring(i + 2, e)
	                            if (img.equalsIgnoreCase('exclaim')) {
	                                img = 'exclamation'
									dflt = 'Exclamation mark'
	                            } else if (img.equalsIgnoreCase('question')) {
	                                img = 'question'
									dflt = 'Question mark'
	                            } else if (img.equalsIgnoreCase('info')) {
	                                img = 'information'
									dflt = 'Information symbol'
	                            } else {
	                                img = null
	                            }
	
	                            if (img) {
	                                sb.append('<img src="')
	                                sb.append("${applicationURI}/images/skin/${img}.png")
	                                sb.append('" alt="')
									sb.append(message(code: "generic.${img}.alt.text", default: dflt, encodeAs: 'HTML'))
									sb.append('" class="borderless"/>')
	                                i = e + 1
	                                continue
	                            }
	                        }
	                    }
	
	                    sb.append(line[i])
	                    break
						
                    default:
                        sb.append(line[i])
                        break
                }
            }
        }

        // If the last char was a \, then add a line break
        if (nextIsLiteral) sb.append('<br/>')

        if (stack) {
            for (int i = stack.size() - 1; i >= 0; i--) {
                switch (stack[i]) {
                    case 'b':
                        sb.append('</b>')
                        break
                    case 'i':
                        sb.append('</i>')
                        break
                    case 'u':
                        sb.append('</u>')
                        break
                    case 's':
                        sb.append('</s>')
                        break
                }
            }
        }

        return sb.toString()
    }

    private peek(line, pos) {
        if (pos + 1 < line.size()) return line[pos + 1]
        return ''
    }

    private push(stack, val) {
        stack << val
    }

    private pop(stack) {
        stack.remove(stack.size() - 1)
    }
}

class MarkupBlock {
    def type
    def lines = []

    MarkupBlock(type) {
        this.type = type
    }

    MarkupBlock(type, line) {
        this.type = type
        lines << line
    }
}