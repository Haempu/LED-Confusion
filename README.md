# LED-Confusion

<h2>Inhalt des Git-Repositorys LED-Confusion</h2>

/Bachelorthesis

  /Bachelorthesis-LED-Mapper.pdf
   - Die Inbetriebnahme des Agents und der Services wird in der Bachelorthesis Kapitel 4.12 beschrieben.

  /Downloads
   - Mobile_Applikationen: APK's der Mobile-Applikationen User Interface und Kamera-Service
   - Standardsoftware: Ausführbare JAR-Dateien für die Java-Applikationen
   - mosquitto.zip: Mosquitto Installation für Windows

  /Projekte
   - Android_Studio: Beinhaltet die Projekte der Mobile-Applikationen User Interface und Kamera-Service
   - Eclipse: Beinhaltet die Java-Projekte zum Agent und zu den Services Dataenverarbeitung, Bildschirm, Led-Strip und Bildumwandlung
   
<h2>Abstract der Bachelorthesis</h2>

<strong>Ausgangslage</strong>

Lichterketten mit LEDs werden immer populärer. Während die Leute jedoch vor zehn Jahren noch damit zufrieden waren, einfach in einer festgelegten Reihenfolge die Farben wechseln zu lassen, wollen sie heute das Motiv gleich selber bestimmen. Dies ist jedoch mit einem erheblichen Aufwand verbunden. Die Konfiguration jeder einzelnen Leuchtquelle in der Lichterkette benötigt technisches Verständnis und ist aufwändig.

<strong>Zielsetzung</strong>

Das Zuweisen und Konfigurieren der einzelnen Leuchtquellen sollte doch eigentlich mit modernen Mitteln in wenigen Schritten möglich sein:
1.	Eine Kamera auf die Lichterszene richten
2.	Mobile-App ausführen und per Knopfdruck die Zuweisung der einzelnen Leuchtquellen starten
3.	Lichterszene virtuell auf der Mobile-App darstellen
4.	Muster zeichnen oder Bilder laden
5.	Virtuelle Szenerie auf die Lichterkette laden

<strong>Umsetzung</strong>

Die einzelnen Teilbereiche des gesamten Systems wie beispielsweise die Mobile-App oder die Funktion für die Zuweisung der einzelnen Leuchtquellen werden in sogenannte Services unterteilt. Sie können auf verschiedenen Geräten, unabhängig von anderen Services laufen und über das MQTT-Protokoll kommunizieren. Das MQTT-Protokoll dient zur Kommunikation und gleichzeitig als Schnittstelle zu einem Agent. Dieser fungiert als Zentrale und verbindet sich mit jeder, von einem Service zur Verfügung gestellten, Schnittstelle. Liegt nun an einer beliebigen Schnittstelle ein Befehl an, zum Beispiel: „Schalte Leuchtquelle Nummer 10 ein“, leitet der Agent die entsprechende Aktion ein. Er meldet auf der Schnittstelle zur Lichterkette: „Schalte Leuchtquelle Nummer 10 ein“. Ist ein Service vorhanden, der diesen Befehl ausführen kann, wird die Leuchtquelle Nummer 10 eingeschaltet.
Dieser modulare Aufbau hat den Vorteil, dass ein einzelner Service durch einen völlig anderen ersetzt werden kann, solange er die entsprechende Schnittstelle bereitstellt. Dadurch kann unser Projekt für die verschiedensten Lichterketten eingesetzt werden. Es muss lediglich ein neuer Service für die entsprechende Hardware geschrieben werden.

<strong>Resultat</strong>

Bei einer Lichterkette mit zahlreichen Leuchtquellen (ca. 300) dauert es doch einige Minuten bis alle komplett zugewiesen sind. Auch ist es, trotz diversen Konfigurationsmöglichkeiten, schwierig, den verschiedenen Lichtverhältnissen Rechnung zu tragen. Mit dem richtigen Lichtverhältnis kann unser System das Zuweisen der einzelnen Leuchtquellen jedoch schnell und zuverlässig umsetzen. Das Darstellen von verschiedenen Motiven ist über die Mobile-App bequem und einfach. Dies ermöglicht auch einem technischen Laien, Bilder schnell und ohne Frust auf seiner Lichterkette darzustellen.

