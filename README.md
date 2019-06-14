# MusicBox-and-Client

Szerver
Készítsd el a MusicBox osztályt, amelynek főprogramja egy szervert indít el a 40000 porton. A szerver dalokat tárol el és játszik vissza; egyszerre tetszőlegesen sok klienst tud kiszolgálni. A szerver a következő fajta szöveges üzeneteket várja:

add <cím>: a megadott című dal feltöltése
a következő sor tartalmazza a dal adatait: a hangokat és azok hosszát
a hangok hossza nyolcadokban van megadva
R jelentése szünet
REP n;m jelentése: a legutóbbi n hangot meg kell ismételni m alkalommal
feltehető, hogy az ismétlésben csak “szokásos” hangok érintettek (R és REP nem)
a hangok alakja a szokásos (pl. C vagy Db), és lehetnek magasabb (F/1, A#/3) vagy alacsonyabb (Bb/-1, D#/-2) oktávban
példa: C 4 E 4 C 4 E 4 G 8 G 8 REP 6;1 C/1 4 B 4 A 4 G 4 F 8 A 8 G 4 F 4 E 4 D 4 C 8 C 8
példa: D 1 D 3 D/1 1 D/1 3 C/1 1 C/1 3 C/1 2 C/1 2 D/1 1 D/1 3 C/1 1 Bb 3 A 4 A 2 R 2 REP 15;1 Bb 4 A 2 G 2 F 1 F 3 E 2 D 2 G 2 G 2 C/1 2 Bb 2 A 4 D/1 2 R 2 C/1 1 Bb 3 A 2 G 2 G 1 A 3 G 2 F 2 A 1 G 3 F# 2 Eb 2 D 4 D 2 R 2
ha már létezik a megadott nevű dal, a feltöltés felülírja
addlyrics <cím>: a megadott című dalhoz szöveg feltöltése
a következő sor tartalmazza a szöveget: minden hanghoz egy szótagot, szóközökkel elválasztva
példa: az első dalhoz bo ci bo ci tar ka se fü le se far ka o da me gyünk lak ni a hol te jet kap ni
a REP részekre értelemszerűen a soron következő szótagok esnek, itt a se fü le se far ka
play <tempó> <transzponálás> <cím>: a megadott című dal lejátszása
a tempó azt adja meg, hogy egy nyolcadhang hány ezredmásodpercig tartson
a transzponálás azt adja meg, hogy hány félhanggal az eredeti fölött/alatt játssza le a rendszer a dalt
a lejátszás menete a következő
a lejátszás kap egy sorszámot, ezt a szerver playing <sorszám> szöveg elküldésével jelzi a kliensnek
a szerver elküldi a kliensnek a dal hangjait a megfelelő ütemezéssel
az elküldött tartalom maga a hang és a dalszöveg megfelelő szótaga: C bo, E ci, …
ha a szótag nem ismert (ha nincsen feltöltve dalszöveg, vagy van, de túl rövid), ??? kerül a helyére
lejátszás közben is feltölthető dalszöveg, ez a következő lejátszott hangtól jelenjen is meg
szünethez sosem tartozik szótag
a dal végét FIN jelzi a kliens számára
ha nem ismert a dal, rögtön FIN-t kap a kliens
change <sorszám> <tempó> <transzponálás>: a megadott sorszámú dal paramétereinek megváltoztatása; a következő hangtól módosítja a lejátszást
a kliens akkor is kiadhatja ezt az utasítást, ha nem felé játssza le a szerver a dalt
a transzponálás paraméter le is hagyható, ekkor 0-nak tekintendő
stop <sorszám>: a megadott sorszámú dalból nem játszódik le több hang a kliens felé; amint lehet, a kliens megkapja a FIN üzenetet


Kliens
Készíts MusicBoxClient klienst, amely képes csatlakozni a portra, és a parancssorban megadott adatokkal play üzenetet küld a szervernek, majd fogadja a dal hangjait/szövegét, és lejátssza/megjeleníti őket.

Két lépést előre meg kell tenni.
A lejátszáshoz először meg kell hívni a MidiSystem osztály getSynthesizer() metódusát.
Ezután meg kell nyitni a kapott Synthesizer objektumot, és ennek csatornái (getChannels()) közül ki kell választani egyet, pl. a legelső a zongora.
Hang képzéséhez a csatornán a megfelelő ütemben meg kell hívni a noteOn() és noteOff() metódusokat.
A metódusok első paramétere adja meg, melyik hang szóljon. A C hang kódja 60, innen minden lépés egy félhangnyi távolságra van: C# kódja 61, Bb/-1 kódja 59, Db/1 kódja 73…
Új dal és dalszöveg feltöltése szokásos konzolos klienssel (PuTTY vagy telnet) tehető meg, de igény szerint szabad hozzá saját kliensprogramot készíteni.
