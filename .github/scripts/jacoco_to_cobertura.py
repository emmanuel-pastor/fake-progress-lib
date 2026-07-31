import sys
import xml.etree.ElementTree as ET

input_path = sys.argv[1]
output_path = sys.argv[2]

tree = ET.parse(input_path)
root = tree.getroot()

cobertura = ET.Element('coverage')
cobertura.set('version', '1')

packages = ET.SubElement(cobertura, 'packages')

for pkg in root.findall('.//package'):
    pkg_el = ET.SubElement(packages, 'package')
    pkg_el.set('name', pkg.get('name', '').replace('/', '.'))
    classes_el = ET.SubElement(pkg_el, 'classes')
    for sf in pkg.findall('sourcefile'):
        cls_el = ET.SubElement(classes_el, 'class')
        cls_el.set('filename', pkg.get('name', '') + '/' + sf.get('name', ''))
        cls_el.set('name', sf.get('name', ''))
        lines_el = ET.SubElement(cls_el, 'lines')
        for line in sf.findall('line'):
            line_el = ET.SubElement(lines_el, 'line')
            line_el.set('number', line.get('nr', '0'))
            line_el.set('hits', '1' if int(line.get('ci', '0')) > 0 else '0')

ET.indent(cobertura)
ET.ElementTree(cobertura).write(output_path, xml_declaration=True, encoding='unicode')
print(f'Cobertura XML written to {output_path}')
