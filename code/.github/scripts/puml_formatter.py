import re
import sys

def process_args(arg_string):
    """
    Converts a string of comma-separated types into named arguments.
    Example: "String,ArrayList<String>" -> "arg0 : String, arg1 : ArrayList<String>"
    """
    if not arg_string.strip():
        return ""
    
    # This is a simple parser. It splits by comma but tries to respect
    # generics (like List<String, Integer>) by temporarily replacing
    # the comma inside <>
    
    generics = re.findall(r"(<.*?>)", arg_string)
    temp_arg_string = arg_string
    placeholders = []
    
    for i, g in enumerate(generics):
        placeholder = f"__GENERIC_{i}__"
        temp_arg_string = temp_arg_string.replace(g, placeholder, 1)
        placeholders.append(g)

    # Now, split by the commas that are left
    parts = temp_arg_string.split(',')
    
    # Restore the generics
    restored_parts = []
    for part in parts:
        restored_part = part
        for i, p in enumerate(placeholders):
            placeholder = f"__GENERIC_{i}__"
            if placeholder in restored_part:
                restored_part = restored_part.replace(placeholder, p, 1)
        restored_parts.append(restored_part.strip())

    # Format with arg names
    named_args = [f"arg{i} : {part}" for i, part in enumerate(restored_parts) if part]
    return ", ".join(named_args)

def convert_puml_formatting(input_text):
    """
    Converts a Puml text block to the "name : Type" convention
    while leaving class/interface names untouched.
    """
    output_lines = []
    
    # Regex for methods: captures (1: visibility, 2: static, 3: stereotype, 4: return, 5: name, 6: args)
    method_regex = re.compile(
        r"^(\s*[\+\-\#\~]\s+)"           # 1: Visibility (+, -, #, ~)
        r"(?:(\{static\})\s*)?"           # 2: Optional {static}
        r"(?:(<<Create>>)\s*)?"          # 3: Optional <<Create>>
        r"(\S+)\s+"                      # 4: Return Type (or Class Name for constructor)
        r"([\w\$]+)"                     # 5: Method Name
        r"\s*\((.*?)\)\s*$"              # 6: Arguments
    )
    
    # Regex for fields: captures (1: visibility, 2: type, 3: name)
    field_regex = re.compile(
        r"^(\s*[\+\-\#\~]\s+)"           # 1: Visibility
        r"(\S+)\s+"                      # 2: Type (e.g., String, List<String>)
        r"([\w\$]+);?$"                  # 3: Field Name
    )

    for line in input_text.splitlines():
        # --- 1. Check for Method ---
        # (Must check before field, as a method line might look like a field)
        method_match = method_regex.search(line)
        if method_match:
            vis, static, create, return_type, name, args = method_match.groups()
            
            # Process arguments
            processed_args = process_args(args)
            
            # Handle static
            static_str = f"{static} " if static else ""
            
            # Handle constructors (where return_type == name and/or <<Create>> is present)
            if create or (return_type == name and not static):
                output_lines.append(f"{vis}{static_str}{name}({processed_args})")
            else:
                output_lines.append(f"{vis}{static_str}{name}({processed_args}) : {return_type}")
            continue
            
        # --- 2. Check for Field ---
        field_match = field_regex.search(line)
        if field_match:
            vis, f_type, f_name = field_match.groups()
            output_lines.append(f"{vis}{f_name} : {f_type}")
            continue

        # --- 3. Keep other lines as-is ---
        # (This will include class/interface definitions, '}', '@startuml', etc.)
        output_lines.append(line)

    return "\n".join(output_lines)

# --- Main execution ---
if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python puml_formatter.py <input_file.puml>")
        sys.exit(1)

    input_file = sys.argv[1]
    
    try:
        with open(input_file, 'r') as f:
            content = f.read()
            
        # Run the conversion
        converted_content = convert_puml_formatting(content)
        
        # Write to the output file
        output_file = input_file.replace('.puml', '_formatted.puml')
        with open(output_file, 'w') as f:
            f.write(converted_content)
            
        print(f"Formatting complete. Output saved to: {output_file}")

    except FileNotFoundError:
        print(f"Error: File not found at {input_file}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"An error occurred: {e}", file=sys.stderr)
        sys.exit(1)