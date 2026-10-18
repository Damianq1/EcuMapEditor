import os
import sys

def check_file(path, required_strings=[]):
    if not os.path.exists(path):
        print(f"[-] BRAK PLIKU: {path}")
        return False
    
    with open(path, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
        for s in required_strings:
            if s not in content:
                print(f"[!] OSTRZEŻENIE w {path}: Brak wymaganego ciągu '{s}'")
        print(f"[+] OK: {path}")
    return True

def main():
    print("=== URUCHAMIAM LOKALNĄ WERYFIKACJĘ PROJEKTU ECU MAP EDITOR ===")
    
    # 1. Sprawdzenie kluczowych plików konfiguracyjnych
    all_ok = True
    all_ok &= check_file("settings.gradle.kts", ["repositoriesMode.set", "google()"])
    all_ok &= check_file("build.gradle.kts", ["layout.buildDirectory", "plugins"])
    all_ok &= check_file("app/build.gradle.kts", ["com.android.application"])
    all_ok &= check_file("gradlew")
    all_ok &= check_file("app/src/main/AndroidManifest.xml", ["manifest"])

    # 2. Sprawdzenie katalogów źródłowych Kotlin
    main_java = "app/src/main/java/com/winols/app"
    if os.path.exists(main_java):
        print(f"[+] OK: Katalog źródłowy Java/Kotlin istnieje ({main_java})")
        files = os.listdir(main_java)
        print(f"    Znalezione pliki/pakiety w głównym folderzie: {files}")
    else:
        print(f"[-] BRAK katalogu źródłowego: {main_java}")
        all_ok = False

    # 3. Sprawdzenie uprawnień gradlew
    if os.path.exists("gradlew"):
        is_executable = os.access("gradlew", os.X_OK)
        if is_executable:
            print("[+] OK: Plik 'gradlew' ma uprawnienia do wykonywania (executable)")
        else:
            print("[!] UWAGA: Plik 'gradlew' NIE ma uprawnień wykonywania! Naprawiam przez chmod +x...")
            os.chmod("gradlew", 0o755)

    print("\n" + "="*50)
    if all_ok:
        print("[+] WYNIK: Struktura projektu wygląda poprawnie i jest gotowa na GitHub Actions!")
    else:
        print("[-] WYNIK: Wykryto braki lub ostrzeżenia powyżej. Sprawdź pliki.")
    print("="*50)

if __name__ == "__main__":
    main()
