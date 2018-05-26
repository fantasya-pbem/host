; RegEx-Template für Fantasya 2 bzw. FCheck2


^(fantasya) [a-z0-9]{1,4} (")[a-zA-Z0-9]+(")$
^(eressea) [a-z0-9]{1,4} (")[a-zA-Z0-9]+(")$
^(partei) [a-z0-9]{1,4} (")[a-zA-Z0-9]+(")$
^(locale) [a-zA-Z0-9]+$
^(region) .+$
^(runde) [0-9]{1,4}$
^(einheit) [a-z0-9]{1,4}$
^(naechster)$

^@?(mache)[n]? (temp) [a-z0-9]{1,4}([ ]+(\/\/).*)?
^@?(ende)([ ]+(\/\/).*)?

^(\/\/).*
^;.*

^@?((vergesse)[n]?|(vergiss)) ((kräuterkunde)|(tarnung)|(burgenbau)|(straßenbau)|(katapultbedienung)|(unterhaltung)|(armbrustschiessen)|(handel)|(kraeuterkunden)|(bogenschiessen)|(holzfaellen)|(religion)|(steuereintreiben)|(reiten)|(segeln)|(ausdauer)|(taktik)|(drachenreiten)|(handeln)|(waffenbau)|(kraeuterkunde)|(bogenbau)|(monsterkampf)|(armbrustschießen)|(steinbau)|(alchemie)|(rüstungsbau)|(speerkampf)|(kräuterkunden)|(wagenbau)|(pferdedressur)|(magie)|(ruestungsbau)|(strassenbau)|(schiffbau)|(wahrnehmung)|(hiebwaffen)|(bergbau)|(bogenschießen)|(spionage)|(holzfällen))([ ]+(\/\/).*)?
^@?((vergesse)[n]?|(vergiss)) [1-9]{1}[0-9]{0,5} ((kräuterkunde)|(tarnung)|(burgenbau)|(straßenbau)|(katapultbedienung)|(unterhaltung)|(armbrustschiessen)|(handel)|(kraeuterkunden)|(bogenschiessen)|(holzfaellen)|(religion)|(steuereintreiben)|(reiten)|(segeln)|(ausdauer)|(taktik)|(drachenreiten)|(handeln)|(waffenbau)|(kraeuterkunde)|(bogenbau)|(monsterkampf)|(armbrustschießen)|(steinbau)|(alchemie)|(rüstungsbau)|(speerkampf)|(kräuterkunden)|(wagenbau)|(pferdedressur)|(magie)|(ruestungsbau)|(strassenbau)|(schiffbau)|(wahrnehmung)|(hiebwaffen)|(bergbau)|(bogenschießen)|(spionage)|(holzfällen))([ ]+(\/\/).*)?

^@?(helfe)[n]? [a-z0-9]{1,4}( nicht)?([ ]+(\/\/).*)?
^@?(helfe)[n]? [a-z0-9]{1,4} (((kaempfe)|(kaempfen))|((gib)|(gibn))|((resourcen)|(resourcenn))|((treiben)|(treibenn))|((handel)|(handeln))|((unterhalte)|(unterhalten))|((kontaktiere)|(kontaktieren))|((steuern)|(steuernn))|((alles)|(allesn))|(ressourcen))( nicht)?([ ]+(\/\/).*)?

^@?(kaempfe)[n]?(( aggressiv)|( vorne)|( fliehe)|( hinten)|( nicht)|( immun)|( vorn))?([ ]+(\/\/).*)?

^@?(kampfzauber) ((angriff)|(verwirrung)|(verteidigung)) ((nicht)|(kein)|(keiner))([ ]+(\/\/).*)?
^(kampfzauber) ((angriff)|(verwirrung)|(verteidigung)) (")?(feuerball)(")? [1-9][0-9]?([ ]+(\/\/).*)?
^(kampfzauber) ((angriff)|(verwirrung)|(verteidigung)) (")?(feuerwalze)(")? [1-9][0-9]?([ ]+(\/\/).*)?
^(kampfzauber) ((angriff)|(verwirrung)|(verteidigung)) (")?(steinschlag)(")? [1-9][0-9]?([ ]+(\/\/).*)?
^(kampfzauber) ((angriff)|(verwirrung)|(verteidigung)) (")?(kleines erdbeben)(")? [1-9][0-9]?([ ]+(\/\/).*)?
^(kampfzauber) ((angriff)|(verwirrung)|(verteidigung)) (")?(sturm)(")? [1-9][0-9]?([ ]+(\/\/).*)?
^(kampfzauber) ((angriff)|(verwirrung)|(verteidigung)) (")?(erdbeben)(")? [1-9][0-9]?([ ]+(\/\/).*)?

^@?((sammel)|(sammeln)|(sammle))(( keine beute)|( beute nicht)|( nicht beute))([ ]+(\/\/).*)?
^@?((sammel)|(sammeln)|(sammle))(( tragbare beute)|( beute massvoll))([ ]+(\/\/).*)?
^@?((sammel)|(sammeln)|(sammle))(( alle beute)|( beute alles)|( beute))([ ]+(\/\/).*)?

^@?(attackiere)[n]?( [a-z0-9]{1,4})+([ ]+(\/\/).*)?
^@?(attackiere)[n]? (partei)( [a-z0-9]{1,4})+([ ]+(\/\/).*)?
^@?(attackiere)[n]? ((vorne)|(hinten))([ ]+(\/\/).*)?
^@?(attackiere)[n]?( gezielt)( [a-z0-9]{1,4})+([ ]+(\/\/).*)?


^@?(set) (person)(en)? [0-9]+([ ]+(\/\/).*)?
^@?(set) (item)(s)? [a-z]+ [0-9]+([ ]+(\/\/).*)?
^@?(set) ((skill)|(talent)) [a-z]+ [0-9]+([ ]+(\/\/).*)?
^@?(set) ((faction)|(party)|(volk)|(partei)) [a-z0-9]{1,4}([ ]+(\/\/).*)?
^@?(set) ((ship)|(schiff)) [a-z]+([ ]+(\/\/).*)?
^@?(set) ((building)|(gebaeude)) [a-z]+([ ]+(\/\/).*)?

^@?(website) .+([ ]+(\/\/).*)?
^@?(homepage) .+([ ]+(\/\/).*)?

^@?(beschreibe)[n]? ((einheit)|(region)|(gebaeude)|(gebäude)|(burg)|(schiff)|(volk)|(partei)|(insel)|(kontinent)) .+$

^@?(benenne)[n]? ((einheit)|(region)|(gebäude)|(gebaeude)|(burg)|(schiff)|(volk)|(partei)|(insel)|(kontinent)) .+$

^@?(bewache)[n]? (nicht)([ ]+(\/\/).*)?

^@?(ursprung) [-]?[0-9]+ [-]?[0-9]+([ ]+(\/\/).*)?

^@?(zeige)[n]? (zauberbuch)([ ]+(\/\/).*)?
^@?(zeige)[n]? .+

^@?(kontaktiere)[n]?( temp)[a-z0-9]{1,4}([ ]+(\/\/).*)?
^@?(kontaktiere)[n]? [a-z0-9]{1,4}([ ]+(\/\/).*)?
^@?(kontaktiere)[n]?( temp)[a-z0-9]{1,4} (permanent)([ ]+(\/\/).*)?
^@?(kontaktiere)[n]? [a-z0-9]{1,4} (permanent)([ ]+(\/\/).*)?

^@?(sortiere)n? (vor) [a-z0-9]{1,4}([ ]+(\/\/).*)?
^@?(sortiere)n? ((nach)|(hinter)) [a-z0-9]{1,4}([ ]+(\/\/).*)?
^@?(sortiere)n? ((vorn)|(vorne)|(anfang)|(erste)|(erster)|(erstes))([ ]+(\/\/).*)?
^@?(sortiere)n? ((ende)|(hinten)|(letzte)|(letzter)|(letztes))([ ]+(\/\/).*)?

^@?(gib) [a-z0-9]{1,4} (")?((juwel)|(juwelen)|(pferd)|(pferde)|(bluetengrumpfe)|(bluetengrumpf)|(dracheneier)|(drachenei)|(flugdracheneier)|(flugdrachenei)|(plattenpanzer)|(kamel)|(kamele)|(eisenschilde)|(eisenschild)|(mastodonpanzer)|(weihrauch)|(runenschwert)|(runenschwerter)|(amulette der heilung)|(amulett der heilung)|(amuletderheilung)|(ballen feinster seide)|(seide)|(wirzblatt)|(wirzblaetter)|(drachenpanzer)|(kettenhemden)|(kettenhemd)|(wagen)|(balsam)|(katapult)|(katapulte)|(alpaka)|(alpakas)|(oele)|(oel)|(gewuerz)|(gewuerze)|(schwert)|(schwerter)|(speere)|(speer)|(pegasus)|(pegasi)|(schildsteine)|(schildstein)|(kriegselefant)|(kriegselefanten)|(ring der kraft)|(ringderkraft)|(ringe der kraft)|(streitaxt)|(streitaexte)|(schnupfniese)|(schnupfnies)|(holzschilde)|(holzschild)|(einhorn)|(einhörner)|(einhoerner)|(armbrueste)|(armbrust)|(greife)|(greif)|(elefantenpanzer)|(kriegshammer)|(silber)|(flachtupfe)|(flachtupf)|(amulette des wahren sehens)|(amuletdessehens)|(amulett des wahren sehens)|(gold)|(mantel der unverletzlichkeit)|(mäntel der unverletzlichkeit)|(maentel der unverletzlichkeit)|(mantelderunverletzlichkeit)|(flugdrache)|(flugdrachen)|(helmder7winde)|(helme der 7 winde)|(helm der 7 winde)|(helme der sieben winde)|(helm der sieben winde)|(trockenwurz)|(trockenwurze)|(silberkiste)|(kriegsmastodon)|(kriegsmastodons)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(eisen)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(grotenolm)|(grotenolme)|(ringderunsichtbarkeit)|(ring der unsichtbarkeit)|(ringe der unsichtbarkeit)|(pelz)|(pelze)|(elefant)|(elefanten)|(boegen)|(bogen)|(holz)|(sumpfkraeuter)|(sumpfkraut)|(greifenei)|(greifeneier)|(stein)|(steine))(")?([ ]+(\/\/).*)?
^@?(liefere) [a-z0-9]{1,4} (")?((juwel)|(juwelen)|(pferd)|(pferde)|(bluetengrumpfe)|(bluetengrumpf)|(dracheneier)|(drachenei)|(flugdracheneier)|(flugdrachenei)|(plattenpanzer)|(kamel)|(kamele)|(eisenschilde)|(eisenschild)|(mastodonpanzer)|(weihrauch)|(runenschwert)|(runenschwerter)|(amulette der heilung)|(amulett der heilung)|(amuletderheilung)|(ballen feinster seide)|(seide)|(wirzblatt)|(wirzblaetter)|(drachenpanzer)|(kettenhemden)|(kettenhemd)|(wagen)|(balsam)|(katapult)|(katapulte)|(alpaka)|(alpakas)|(oele)|(oel)|(gewuerz)|(gewuerze)|(schwert)|(schwerter)|(speere)|(speer)|(pegasus)|(pegasi)|(schildsteine)|(schildstein)|(kriegselefant)|(kriegselefanten)|(ring der kraft)|(ringderkraft)|(ringe der kraft)|(streitaxt)|(streitaexte)|(schnupfniese)|(schnupfnies)|(holzschilde)|(holzschild)|(einhorn)|(einhörner)|(einhoerner)|(armbrueste)|(armbrust)|(greife)|(greif)|(elefantenpanzer)|(kriegshammer)|(silber)|(flachtupfe)|(flachtupf)|(amulette des wahren sehens)|(amuletdessehens)|(amulett des wahren sehens)|(gold)|(mantel der unverletzlichkeit)|(mäntel der unverletzlichkeit)|(maentel der unverletzlichkeit)|(mantelderunverletzlichkeit)|(flugdrache)|(flugdrachen)|(helmder7winde)|(helme der 7 winde)|(helm der 7 winde)|(helme der sieben winde)|(helm der sieben winde)|(trockenwurz)|(trockenwurze)|(silberkiste)|(kriegsmastodon)|(kriegsmastodons)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(eisen)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(grotenolm)|(grotenolme)|(ringderunsichtbarkeit)|(ring der unsichtbarkeit)|(ringe der unsichtbarkeit)|(pelz)|(pelze)|(elefant)|(elefanten)|(boegen)|(bogen)|(holz)|(sumpfkraeuter)|(sumpfkraut)|(greifenei)|(greifeneier)|(stein)|(steine))(")?([ ]+(\/\/).*)?
^@?(gib) (temp) [a-z0-9]{1,4} (")?((juwel)|(juwelen)|(pferd)|(pferde)|(bluetengrumpfe)|(bluetengrumpf)|(dracheneier)|(drachenei)|(flugdracheneier)|(flugdrachenei)|(plattenpanzer)|(kamel)|(kamele)|(eisenschilde)|(eisenschild)|(mastodonpanzer)|(weihrauch)|(runenschwert)|(runenschwerter)|(amulette der heilung)|(amulett der heilung)|(amuletderheilung)|(ballen feinster seide)|(seide)|(wirzblatt)|(wirzblaetter)|(drachenpanzer)|(kettenhemden)|(kettenhemd)|(wagen)|(balsam)|(katapult)|(katapulte)|(alpaka)|(alpakas)|(oele)|(oel)|(gewuerz)|(gewuerze)|(schwert)|(schwerter)|(speere)|(speer)|(pegasus)|(pegasi)|(schildsteine)|(schildstein)|(kriegselefant)|(kriegselefanten)|(ring der kraft)|(ringderkraft)|(ringe der kraft)|(streitaxt)|(streitaexte)|(schnupfniese)|(schnupfnies)|(holzschilde)|(holzschild)|(einhorn)|(einhörner)|(einhoerner)|(armbrueste)|(armbrust)|(greife)|(greif)|(elefantenpanzer)|(kriegshammer)|(silber)|(flachtupfe)|(flachtupf)|(amulette des wahren sehens)|(amuletdessehens)|(amulett des wahren sehens)|(gold)|(mantel der unverletzlichkeit)|(mäntel der unverletzlichkeit)|(maentel der unverletzlichkeit)|(mantelderunverletzlichkeit)|(flugdrache)|(flugdrachen)|(helmder7winde)|(helme der 7 winde)|(helm der 7 winde)|(helme der sieben winde)|(helm der sieben winde)|(trockenwurz)|(trockenwurze)|(silberkiste)|(kriegsmastodon)|(kriegsmastodons)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(eisen)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(grotenolm)|(grotenolme)|(ringderunsichtbarkeit)|(ring der unsichtbarkeit)|(ringe der unsichtbarkeit)|(pelz)|(pelze)|(elefant)|(elefanten)|(boegen)|(bogen)|(holz)|(sumpfkraeuter)|(sumpfkraut)|(greifenei)|(greifeneier)|(stein)|(steine))(")?([ ]+(\/\/).*)?
^@?(liefere) (temp) [a-z0-9]{1,4} (")?((juwel)|(juwelen)|(pferd)|(pferde)|(bluetengrumpfe)|(bluetengrumpf)|(dracheneier)|(drachenei)|(flugdracheneier)|(flugdrachenei)|(plattenpanzer)|(kamel)|(kamele)|(eisenschilde)|(eisenschild)|(mastodonpanzer)|(weihrauch)|(runenschwert)|(runenschwerter)|(amulette der heilung)|(amulett der heilung)|(amuletderheilung)|(ballen feinster seide)|(seide)|(wirzblatt)|(wirzblaetter)|(drachenpanzer)|(kettenhemden)|(kettenhemd)|(wagen)|(balsam)|(katapult)|(katapulte)|(alpaka)|(alpakas)|(oele)|(oel)|(gewuerz)|(gewuerze)|(schwert)|(schwerter)|(speere)|(speer)|(pegasus)|(pegasi)|(schildsteine)|(schildstein)|(kriegselefant)|(kriegselefanten)|(ring der kraft)|(ringderkraft)|(ringe der kraft)|(streitaxt)|(streitaexte)|(schnupfniese)|(schnupfnies)|(holzschilde)|(holzschild)|(einhorn)|(einhörner)|(einhoerner)|(armbrueste)|(armbrust)|(greife)|(greif)|(elefantenpanzer)|(kriegshammer)|(silber)|(flachtupfe)|(flachtupf)|(amulette des wahren sehens)|(amuletdessehens)|(amulett des wahren sehens)|(gold)|(mantel der unverletzlichkeit)|(mäntel der unverletzlichkeit)|(maentel der unverletzlichkeit)|(mantelderunverletzlichkeit)|(flugdrache)|(flugdrachen)|(helmder7winde)|(helme der 7 winde)|(helm der 7 winde)|(helme der sieben winde)|(helm der sieben winde)|(trockenwurz)|(trockenwurze)|(silberkiste)|(kriegsmastodon)|(kriegsmastodons)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(eisen)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(grotenolm)|(grotenolme)|(ringderunsichtbarkeit)|(ring der unsichtbarkeit)|(ringe der unsichtbarkeit)|(pelz)|(pelze)|(elefant)|(elefanten)|(boegen)|(bogen)|(holz)|(sumpfkraeuter)|(sumpfkraut)|(greifenei)|(greifeneier)|(stein)|(steine))(")?([ ]+(\/\/).*)?
^@?(gib) [a-z0-9]{1,4} [0-9]+ (")?((juwel)|(juwelen)|(pferd)|(pferde)|(bluetengrumpfe)|(bluetengrumpf)|(dracheneier)|(drachenei)|(flugdracheneier)|(flugdrachenei)|(plattenpanzer)|(kamel)|(kamele)|(eisenschilde)|(eisenschild)|(mastodonpanzer)|(weihrauch)|(runenschwert)|(runenschwerter)|(amulette der heilung)|(amulett der heilung)|(amuletderheilung)|(ballen feinster seide)|(seide)|(wirzblatt)|(wirzblaetter)|(drachenpanzer)|(kettenhemden)|(kettenhemd)|(wagen)|(balsam)|(katapult)|(katapulte)|(alpaka)|(alpakas)|(oele)|(oel)|(gewuerz)|(gewuerze)|(schwert)|(schwerter)|(speere)|(speer)|(pegasus)|(pegasi)|(schildsteine)|(schildstein)|(kriegselefant)|(kriegselefanten)|(ring der kraft)|(ringderkraft)|(ringe der kraft)|(streitaxt)|(streitaexte)|(schnupfniese)|(schnupfnies)|(holzschilde)|(holzschild)|(einhorn)|(einhörner)|(einhoerner)|(armbrueste)|(armbrust)|(greife)|(greif)|(elefantenpanzer)|(kriegshammer)|(silber)|(flachtupfe)|(flachtupf)|(amulette des wahren sehens)|(amuletdessehens)|(amulett des wahren sehens)|(gold)|(mantel der unverletzlichkeit)|(mäntel der unverletzlichkeit)|(maentel der unverletzlichkeit)|(mantelderunverletzlichkeit)|(flugdrache)|(flugdrachen)|(helmder7winde)|(helme der 7 winde)|(helm der 7 winde)|(helme der sieben winde)|(helm der sieben winde)|(trockenwurz)|(trockenwurze)|(silberkiste)|(kriegsmastodon)|(kriegsmastodons)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(eisen)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(grotenolm)|(grotenolme)|(ringderunsichtbarkeit)|(ring der unsichtbarkeit)|(ringe der unsichtbarkeit)|(pelz)|(pelze)|(elefant)|(elefanten)|(boegen)|(bogen)|(holz)|(sumpfkraeuter)|(sumpfkraut)|(greifenei)|(greifeneier)|(stein)|(steine))(")?( permanent)?([ ]+(\/\/).*)?
^@?(liefere) [a-z0-9]{1,4} [0-9]+ (")?((juwel)|(juwelen)|(pferd)|(pferde)|(bluetengrumpfe)|(bluetengrumpf)|(dracheneier)|(drachenei)|(flugdracheneier)|(flugdrachenei)|(plattenpanzer)|(kamel)|(kamele)|(eisenschilde)|(eisenschild)|(mastodonpanzer)|(weihrauch)|(runenschwert)|(runenschwerter)|(amulette der heilung)|(amulett der heilung)|(amuletderheilung)|(ballen feinster seide)|(seide)|(wirzblatt)|(wirzblaetter)|(drachenpanzer)|(kettenhemden)|(kettenhemd)|(wagen)|(balsam)|(katapult)|(katapulte)|(alpaka)|(alpakas)|(oele)|(oel)|(gewuerz)|(gewuerze)|(schwert)|(schwerter)|(speere)|(speer)|(pegasus)|(pegasi)|(schildsteine)|(schildstein)|(kriegselefant)|(kriegselefanten)|(ring der kraft)|(ringderkraft)|(ringe der kraft)|(streitaxt)|(streitaexte)|(schnupfniese)|(schnupfnies)|(holzschilde)|(holzschild)|(einhorn)|(einhörner)|(einhoerner)|(armbrueste)|(armbrust)|(greife)|(greif)|(elefantenpanzer)|(kriegshammer)|(silber)|(flachtupfe)|(flachtupf)|(amulette des wahren sehens)|(amuletdessehens)|(amulett des wahren sehens)|(gold)|(mantel der unverletzlichkeit)|(mäntel der unverletzlichkeit)|(maentel der unverletzlichkeit)|(mantelderunverletzlichkeit)|(flugdrache)|(flugdrachen)|(helmder7winde)|(helme der 7 winde)|(helm der 7 winde)|(helme der sieben winde)|(helm der sieben winde)|(trockenwurz)|(trockenwurze)|(silberkiste)|(kriegsmastodon)|(kriegsmastodons)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(eisen)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(grotenolm)|(grotenolme)|(ringderunsichtbarkeit)|(ring der unsichtbarkeit)|(ringe der unsichtbarkeit)|(pelz)|(pelze)|(elefant)|(elefanten)|(boegen)|(bogen)|(holz)|(sumpfkraeuter)|(sumpfkraut)|(greifenei)|(greifeneier)|(stein)|(steine))(")?([ ]+(\/\/).*)?
^@?(gib) (temp) [a-z0-9]{1,4} [0-9]+ (")?((juwel)|(juwelen)|(pferd)|(pferde)|(bluetengrumpfe)|(bluetengrumpf)|(dracheneier)|(drachenei)|(flugdracheneier)|(flugdrachenei)|(plattenpanzer)|(kamel)|(kamele)|(eisenschilde)|(eisenschild)|(mastodonpanzer)|(weihrauch)|(runenschwert)|(runenschwerter)|(amulette der heilung)|(amulett der heilung)|(amuletderheilung)|(ballen feinster seide)|(seide)|(wirzblatt)|(wirzblaetter)|(drachenpanzer)|(kettenhemden)|(kettenhemd)|(wagen)|(balsam)|(katapult)|(katapulte)|(alpaka)|(alpakas)|(oele)|(oel)|(gewuerz)|(gewuerze)|(schwert)|(schwerter)|(speere)|(speer)|(pegasus)|(pegasi)|(schildsteine)|(schildstein)|(kriegselefant)|(kriegselefanten)|(ring der kraft)|(ringderkraft)|(ringe der kraft)|(streitaxt)|(streitaexte)|(schnupfniese)|(schnupfnies)|(holzschilde)|(holzschild)|(einhorn)|(einhörner)|(einhoerner)|(armbrueste)|(armbrust)|(greife)|(greif)|(elefantenpanzer)|(kriegshammer)|(silber)|(flachtupfe)|(flachtupf)|(amulette des wahren sehens)|(amuletdessehens)|(amulett des wahren sehens)|(gold)|(mantel der unverletzlichkeit)|(mäntel der unverletzlichkeit)|(maentel der unverletzlichkeit)|(mantelderunverletzlichkeit)|(flugdrache)|(flugdrachen)|(helmder7winde)|(helme der 7 winde)|(helm der 7 winde)|(helme der sieben winde)|(helm der sieben winde)|(trockenwurz)|(trockenwurze)|(silberkiste)|(kriegsmastodon)|(kriegsmastodons)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(eisen)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(grotenolm)|(grotenolme)|(ringderunsichtbarkeit)|(ring der unsichtbarkeit)|(ringe der unsichtbarkeit)|(pelz)|(pelze)|(elefant)|(elefanten)|(boegen)|(bogen)|(holz)|(sumpfkraeuter)|(sumpfkraut)|(greifenei)|(greifeneier)|(stein)|(steine))(")?( permanent)?([ ]+(\/\/).*)?
^@?(liefere) (temp) [a-z0-9]{1,4} [0-9]+ (")?((juwel)|(juwelen)|(pferd)|(pferde)|(bluetengrumpfe)|(bluetengrumpf)|(dracheneier)|(drachenei)|(flugdracheneier)|(flugdrachenei)|(plattenpanzer)|(kamel)|(kamele)|(eisenschilde)|(eisenschild)|(mastodonpanzer)|(weihrauch)|(runenschwert)|(runenschwerter)|(amulette der heilung)|(amulett der heilung)|(amuletderheilung)|(ballen feinster seide)|(seide)|(wirzblatt)|(wirzblaetter)|(drachenpanzer)|(kettenhemden)|(kettenhemd)|(wagen)|(balsam)|(katapult)|(katapulte)|(alpaka)|(alpakas)|(oele)|(oel)|(gewuerz)|(gewuerze)|(schwert)|(schwerter)|(speere)|(speer)|(pegasus)|(pegasi)|(schildsteine)|(schildstein)|(kriegselefant)|(kriegselefanten)|(ring der kraft)|(ringderkraft)|(ringe der kraft)|(streitaxt)|(streitaexte)|(schnupfniese)|(schnupfnies)|(holzschilde)|(holzschild)|(einhorn)|(einhörner)|(einhoerner)|(armbrueste)|(armbrust)|(greife)|(greif)|(elefantenpanzer)|(kriegshammer)|(silber)|(flachtupfe)|(flachtupf)|(amulette des wahren sehens)|(amuletdessehens)|(amulett des wahren sehens)|(gold)|(mantel der unverletzlichkeit)|(mäntel der unverletzlichkeit)|(maentel der unverletzlichkeit)|(mantelderunverletzlichkeit)|(flugdrache)|(flugdrachen)|(helmder7winde)|(helme der 7 winde)|(helm der 7 winde)|(helme der sieben winde)|(helm der sieben winde)|(trockenwurz)|(trockenwurze)|(silberkiste)|(kriegsmastodon)|(kriegsmastodons)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(eisen)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(grotenolm)|(grotenolme)|(ringderunsichtbarkeit)|(ring der unsichtbarkeit)|(ringe der unsichtbarkeit)|(pelz)|(pelze)|(elefant)|(elefanten)|(boegen)|(bogen)|(holz)|(sumpfkraeuter)|(sumpfkraut)|(greifenei)|(greifeneier)|(stein)|(steine))(")?([ ]+(\/\/).*)?
^@?(gib) (bauern) ((pferd)|(pferde)|(kamel)|(kamele)|(alpaka)|(alpakas)|(pegasus)|(pegasi)|(einhorn)|(einhörner)|(einhoerner)|(greife)|(greif)|(flugdrache)|(flugdrachen)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(elefant)|(elefanten))([ ]+(\/\/).*)?
^@?(liefere) (bauern) ((pferd)|(pferde)|(kamel)|(kamele)|(alpaka)|(alpakas)|(pegasus)|(pegasi)|(einhorn)|(einhörner)|(einhoerner)|(greife)|(greif)|(flugdrache)|(flugdrachen)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(elefant)|(elefanten))([ ]+(\/\/).*)?
^@?(gib) (bauern) [0-9]+ ((pferd)|(pferde)|(kamel)|(kamele)|(alpaka)|(alpakas)|(pegasus)|(pegasi)|(einhorn)|(einhörner)|(einhoerner)|(greife)|(greif)|(flugdrache)|(flugdrachen)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(elefant)|(elefanten))([ ]+(\/\/).*)?
^@?(liefere) (bauern) [0-9]+ ((pferd)|(pferde)|(kamel)|(kamele)|(alpaka)|(alpakas)|(pegasus)|(pegasi)|(einhorn)|(einhörner)|(einhoerner)|(greife)|(greif)|(flugdrache)|(flugdrachen)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(elefant)|(elefanten))([ ]+(\/\/).*)?
^@?(gib) [a-z0-9]{1,4} (alles)([ ]+(\/\/).*)?
^@?(liefere) [a-z0-9]{1,4} (alles)([ ]+(\/\/).*)?
^@?(gib) (temp) [a-z0-9]{1,4} (alles)([ ]+(\/\/).*)?
^@?(liefere) (temp) [a-z0-9]{1,4} (alles)([ ]+(\/\/).*)?
^@?(gib) [a-z0-9]{1,4} [0-9]+ (person)(en)?([ ]+(\/\/).*)?
^@?(liefere) [a-z0-9]{1,4} [0-9]+ (person)(en)?([ ]+(\/\/).*)?
^@?(gib) (temp) [a-z0-9]{1,4} [0-9]+ (person)(en)?([ ]+(\/\/).*)?
^@?(liefere) (temp) [a-z0-9]{1,4} [0-9]+ (person)(en)?([ ]+(\/\/).*)?
^@?(gib) (bauern) (person)(en)?([ ]+(\/\/).*)?
^@?(liefere) (bauern) (person)(en)?([ ]+(\/\/).*)?
^@?(gib) (bauern) [0-9]+ (person)(en)?([ ]+(\/\/).*)?
^@?(liefere) (bauern) [0-9]+ (person)(en)?([ ]+(\/\/).*)?
^@?(gib) [a-z0-9]{1,4} einheit([ ]+(\/\/).*)?
^@?(liefere) [a-z0-9]{1,4} einheit([ ]+(\/\/).*)?
^@?(gib) (bauern) einheit([ ]+(\/\/).*)?
^@?(liefere) (bauern) einheit([ ]+(\/\/).*)?
^@?(gib) [a-z0-9]{1,4} (zauberbuch)([ ]+(\/\/).*)?
^@?(liefere) [a-z0-9]{1,4} (zauberbuch)([ ]+(\/\/).*)?
^@?(gib) (temp) [a-z0-9]{1,4} (zauberbuch)([ ]+(\/\/).*)?
^@?(liefere) (temp) [a-z0-9]{1,4} (zauberbuch)([ ]+(\/\/).*)?
^@?(gib) [a-z0-9]{1,4}( zauberbuch)? (")?((provokation der titanen)|(meister der platten)|(meister des schiffs)|(hammer der götter)|(feuerball)|(feuerwalze)|(luftreise)|(steinschlag)|(meister der wagen)|(fernsicht)|(klauen der tiefe)|(guter wind)|(kleines erdbeben)|(voodoo)|(hain der 1000 eichen)|(meister der resourcen)|(sturm)|(segen der göttin)|(erdbeben)|(meister der schmiede))(")?([ ]+(\/\/).*)?
^@?(liefere) [a-z0-9]{1,4}( zauberbuch)? (")?((provokation der titanen)|(meister der platten)|(meister des schiffs)|(hammer der götter)|(feuerball)|(feuerwalze)|(luftreise)|(steinschlag)|(meister der wagen)|(fernsicht)|(klauen der tiefe)|(guter wind)|(kleines erdbeben)|(voodoo)|(hain der 1000 eichen)|(meister der resourcen)|(sturm)|(segen der göttin)|(erdbeben)|(meister der schmiede))(")?([ ]+(\/\/).*)?
^@?(gib) (temp) [a-z0-9]{1,4}( zauberbuch)? (")?((provokation der titanen)|(meister der platten)|(meister des schiffs)|(hammer der götter)|(feuerball)|(feuerwalze)|(luftreise)|(steinschlag)|(meister der wagen)|(fernsicht)|(klauen der tiefe)|(guter wind)|(kleines erdbeben)|(voodoo)|(hain der 1000 eichen)|(meister der resourcen)|(sturm)|(segen der göttin)|(erdbeben)|(meister der schmiede))(")?([ ]+(\/\/).*)?
^@?(liefere) (temp) [a-z0-9]{1,4}( zauberbuch)? (")?((provokation der titanen)|(meister der platten)|(meister des schiffs)|(hammer der götter)|(feuerball)|(feuerwalze)|(luftreise)|(steinschlag)|(meister der wagen)|(fernsicht)|(klauen der tiefe)|(guter wind)|(kleines erdbeben)|(voodoo)|(hain der 1000 eichen)|(meister der resourcen)|(sturm)|(segen der göttin)|(erdbeben)|(meister der schmiede))(")?([ ]+(\/\/).*)?

^@?(nummer) ((einheit)|(gebaeude)|(gebäude)|(burg)|(schiff)|(volk)|(partei)) [a-z0-9]{1,4}([ ]+(\/\/).*)?

^@?(tarne)[n]? (einheit)([ ]+(\/\/).*)?
^@?(tarne)[n]? (einheit) (nicht)([ ]+(\/\/).*)?
^@?(tarne)[n]? ((volk)|(partei)) [a-z0-9]{1,4}([ ]+(\/\/).*)?
^@?(tarne)[n]? ((volk)|(partei)) (nicht)([ ]+(\/\/).*)?
^@?(tarne)[n]? ((volk)|(partei))([ ]+(\/\/).*)?
^@?(tarne)[n]? (rasse) ((goblin)|(ork)|(dragonfly)|(krake)|(puschkin)|(zombie)|(zwerg)|(echse)|(halbling)|(troll)|(greif)|(hoellenhund)|(mensch)|(kobold)|(elf)|(aquaner))([ ]+(\/\/).*)?
^@?(tarne)[n]? (rasse) (nicht)([ ]+(\/\/).*)?
^@?(tarne)[n]? (nicht)([ ]+(\/\/).*)?

^@?((praefix)|(präfix)|(prefix)) (")?(.*)(")?([ ]+(\/\/).*)?
^@?((praefix)|(präfix)|(prefix))[ ]*((\/\/).*)?

^@?(steuer)(n)? ((100)|([0-9]{1,2}))([ ]+(\/\/).*)?
^@?(steuer)(n)? ((100)|([0-9]{1,2})) [a-z0-9]{1,4}([ ]+(\/\/).*)?

^@?(rekrutiere)[n]? [1-9][0-9]*([ ]+(\/\/).*)?

^(stirb) (")?.+(")?([ ]+(\/\/).*)?

^@?(betrete)[n]? ((gebaeude)|(gebäude)|(burg)) [a-z0-9]{1,4}([ ]+(\/\/).*)?
^@?(betrete)[n]? (schiff) [a-z0-9]{1,4}([ ]+(\/\/).*)?
^@?(betrete)[n]? ((hoehle)|(höhle)) [a-z0-9]{1,4}([ ]+(\/\/).*)?

^@?(gib) [a-z0-9]{1,4} (kommando)([ ]+(\/\/).*)?
^@?(gib) (temp) [a-z0-9]{1,4} (kommando)([ ]+(\/\/).*)?

^@?(verlasse)[n]?(( schiff)|( gebaeude)|( gebäude))?([ ]+(\/\/).*)?

^(zauber)[en]? (((")?(provokation der titanen)(")?)|("?(meister der platten)"? [a-z0-9]{1,4}( [0-9]+)?)|("?(meister des schiffs)"? [a-z0-9]{1,4}( [0-9]+)?)|((")?(hammer der götter)(")?)|((")?(hammer der goetter)(")?)|((")?(feuerball)(")? [1-9][0-9]?)|((")?(feuerwalze)(")? [1-9][0-9]?)|("?(luftreise)"? [-+]?[0-9]+ [-+]?[0-9]+)|((")?(steinschlag)(")? [1-9][0-9]?)|("?(meister der wagen)"? [a-z0-9]{1,4}( [0-9]+)?)|((")?(fernsicht)(")? [-+]?[0-9]+ [-+]?[0-9])|(("klauen der tiefe") [0-9]+)|("?(guter wind)"? [a-z0-9]{1,4}( [0-9]+)?)|((")?(kleines erdbeben)(")? [1-9][0-9]?)|(("voodoo") [a-z0-9]{1,4} (".*"))|("?(hain der 1000 eichen)"?( [0-9]+))|("?(meister der resourcen)"? [a-z0-9]{1,4}( [0-9]+)?)|((")?(sturm)(")? [1-9][0-9]?)|((")?(segen der göttin)(")?)|((")?(segen der goettin)(")?)|((")?(erdbeben)(")? [1-9][0-9]?)|("?(meister der schmiede)"? [a-z0-9]{1,4}( [0-9]+)?))([ ]+(\/\/).*)?

^(zerstoere)[n]?([ ]+(\/\/).*)?
^(zerstoere)[n]? (strasse) (nw|no|o|so|sw|w)([ ]+(\/\/).*)?

^@?(spioniere)[n]? [a-z0-9]{1,4}([ ]+(\/\/).*)?

^(lehre)[n]? .+([ ]+(\/\/).*)?

^(lerne)[n]? ((drachenreiten)|(katapultbedienung)|(taktik)|(bogenschießen)|(bogenschiessen)|(reiten)|(bergbau)|(ausdauer)|(wagenbau)|(tarnung)|(pferdedressur)|(unterhaltung)|(steinbau)|(schiffbau)|(ruestungsbau)|(rüstungsbau)|(segeln)|(hiebwaffen)|(bogenbau)|(wahrnehmung)|(alchemie)|(handeln)|(handel)|(religion)|(holzfaellen)|(holzfällen)|(armbrustschießen)|(armbrustschiessen)|(waffenbau)|(spionage)|(monsterkampf)|(strassenbau)|(straßenbau)|(burgenbau)|(kräuterkunde)|(kräuterkunden)|(kraeuterkunde)|(kraeuterkunden)|(speerkampf)|(magie)|(steuereintreiben))([ ]+(\/\/).*)?
^(lerne)[n]? ((drachenreiten)|(katapultbedienung)|(taktik)|(bogenschießen)|(bogenschiessen)|(reiten)|(bergbau)|(ausdauer)|(wagenbau)|(tarnung)|(pferdedressur)|(unterhaltung)|(steinbau)|(schiffbau)|(ruestungsbau)|(rüstungsbau)|(segeln)|(hiebwaffen)|(bogenbau)|(wahrnehmung)|(alchemie)|(handeln)|(handel)|(religion)|(holzfaellen)|(holzfällen)|(armbrustschießen)|(armbrustschiessen)|(waffenbau)|(spionage)|(monsterkampf)|(strassenbau)|(straßenbau)|(burgenbau)|(kräuterkunde)|(kräuterkunden)|(kraeuterkunde)|(kraeuterkunden)|(speerkampf)|(magie)|(steuereintreiben)) (t|tw)?[1-9]{1}[0-9]{0,2}([ ]+(\/\/).*)?

^(belager)[en]? [a-z0-9]{1,4}([ ]+(\/\/).*)?

^(mache)[n]? ((juwel)|(juwelen)|(pferd)|(pferde)|(bluetengrumpfe)|(bluetengrumpf)|(dracheneier)|(drachenei)|(flugdracheneier)|(flugdrachenei)|(plattenpanzer)|(kamel)|(kamele)|(eisenschilde)|(eisenschild)|(mastodonpanzer)|(weihrauch)|(runenschwert)|(runenschwerter)|(amulette der heilung)|(amulett der heilung)|(amuletderheilung)|(ballen feinster seide)|(seide)|(wirzblatt)|(wirzblaetter)|(drachenpanzer)|(kettenhemden)|(kettenhemd)|(wagen)|(balsam)|(katapult)|(katapulte)|(alpaka)|(alpakas)|(oele)|(oel)|(gewuerz)|(gewuerze)|(schwert)|(schwerter)|(speere)|(speer)|(pegasus)|(pegasi)|(schildsteine)|(schildstein)|(kriegselefant)|(kriegselefanten)|(ring der kraft)|(ringderkraft)|(ringe der kraft)|(streitaxt)|(streitaexte)|(schnupfniese)|(schnupfnies)|(holzschilde)|(holzschild)|(einhorn)|(einhörner)|(einhoerner)|(armbrueste)|(armbrust)|(greife)|(greif)|(elefantenpanzer)|(kriegshammer)|(silber)|(flachtupfe)|(flachtupf)|(amulette des wahren sehens)|(amuletdessehens)|(amulett des wahren sehens)|(gold)|(mantel der unverletzlichkeit)|(mäntel der unverletzlichkeit)|(maentel der unverletzlichkeit)|(mantelderunverletzlichkeit)|(flugdrache)|(flugdrachen)|(helmder7winde)|(helme der 7 winde)|(helm der 7 winde)|(helme der sieben winde)|(helm der sieben winde)|(trockenwurz)|(trockenwurze)|(silberkiste)|(kriegsmastodon)|(kriegsmastodons)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(eisen)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(grotenolm)|(grotenolme)|(ringderunsichtbarkeit)|(ring der unsichtbarkeit)|(ringe der unsichtbarkeit)|(pelz)|(pelze)|(elefant)|(elefanten)|(boegen)|(bogen)|(holz)|(sumpfkraeuter)|(sumpfkraut)|(greifenei)|(greifeneier)|(stein)|(steine))([ ]+(\/\/).*)?
^(mache)[n]? ([0-9]+) ((juwel)|(juwelen)|(pferd)|(pferde)|(bluetengrumpfe)|(bluetengrumpf)|(dracheneier)|(drachenei)|(flugdracheneier)|(flugdrachenei)|(plattenpanzer)|(kamel)|(kamele)|(eisenschilde)|(eisenschild)|(mastodonpanzer)|(weihrauch)|(runenschwert)|(runenschwerter)|(amulette der heilung)|(amulett der heilung)|(amuletderheilung)|(ballen feinster seide)|(seide)|(wirzblatt)|(wirzblaetter)|(drachenpanzer)|(kettenhemden)|(kettenhemd)|(wagen)|(balsam)|(katapult)|(katapulte)|(alpaka)|(alpakas)|(oele)|(oel)|(gewuerz)|(gewuerze)|(schwert)|(schwerter)|(speere)|(speer)|(pegasus)|(pegasi)|(schildsteine)|(schildstein)|(kriegselefant)|(kriegselefanten)|(ring der kraft)|(ringderkraft)|(ringe der kraft)|(streitaxt)|(streitaexte)|(schnupfniese)|(schnupfnies)|(holzschilde)|(holzschild)|(einhorn)|(einhörner)|(einhoerner)|(armbrueste)|(armbrust)|(greife)|(greif)|(elefantenpanzer)|(kriegshammer)|(silber)|(flachtupfe)|(flachtupf)|(amulette des wahren sehens)|(amuletdessehens)|(amulett des wahren sehens)|(gold)|(mantel der unverletzlichkeit)|(mäntel der unverletzlichkeit)|(maentel der unverletzlichkeit)|(mantelderunverletzlichkeit)|(flugdrache)|(flugdrachen)|(helmder7winde)|(helme der 7 winde)|(helm der 7 winde)|(helme der sieben winde)|(helm der sieben winde)|(trockenwurz)|(trockenwurze)|(silberkiste)|(kriegsmastodon)|(kriegsmastodons)|(mastodons)|(mastodon)|(mastodonten)|(zotte)|(zotten)|(eisen)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(grotenolm)|(grotenolme)|(ringderunsichtbarkeit)|(ring der unsichtbarkeit)|(ringe der unsichtbarkeit)|(pelz)|(pelze)|(elefant)|(elefanten)|(boegen)|(bogen)|(holz)|(sumpfkraeuter)|(sumpfkraut)|(greifenei)|(greifeneier)|(stein)|(steine))([ ]+(\/\/).*)?
^(mache)[n]? (strasse) (nw|no|o|so|sw|w|nordwesten|nordosten|osten|suedosten|suedwesten|westen)([ ]+(\/\/).*)?
^(mache)[n]? ((galeone)|(langboot)|(tireme)|(boot)|(karavelle)|(drachenschiff)|(plm[a-z]*))([ ]+(\/\/).*)?
^(mache)[n]? (schiff) [a-z0-9]{1,4}([ ]+(\/\/).*)?
^(mache)[n]? ((gebäude)|(gebaeude)|(burg)|(holzfällerhütte)|(holzfällerhütten)|(holzfaellerhuetten)|(holzfaellerhuette)|(leuchtturm)|(leuchttürme)|(höhle)|(höhlen)|(höhle)|(höhlen)|(kirche)|(kirchen)|(tempel)|(bergwerk)|(bergwerke)|(seehafen)|(seehäfen)|(ruinen)|(ruine)|(höhle)|(höhlen)|(schmieden)|(schmiede)|(steingruben)|(steingrube)|(burgen)|(burg)|(kuechen)|(kueche)|(küchen)|(küche)|(höhle)|(höhlen)|(werkstätten)|(werkstatt)|(steinbrüche)|(steinbruch)|(höhle)|(höhlen)|(saegewerk)|(sägewerke)|(sägewerk)|(saegewerke)|(hafen)|(häfen)|(höhle)|(höhlen)|(schiffswerft)|(schiffswerften)|(minen)|(mine)|(höhle)|(höhlen)|(kathedrale)|(kathedralen)|(steuerturm)|(steuertürme)|(sattlerei)|(sattlereien)|(steg)|(stege)|(monument)|(monumente)|(wegweiser))([ ]+(\/\/).*)?
^(mache)[n]? ((gebäude)|(gebaeude)|(burg)|(holzfällerhütte)|(holzfällerhütten)|(holzfaellerhuetten)|(holzfaellerhuette)|(leuchtturm)|(leuchttürme)|(höhle)|(höhlen)|(höhle)|(höhlen)|(kirche)|(kirchen)|(tempel)|(bergwerk)|(bergwerke)|(seehafen)|(seehäfen)|(ruinen)|(ruine)|(höhle)|(höhlen)|(schmieden)|(schmiede)|(steingruben)|(steingrube)|(burgen)|(burg)|(kuechen)|(kueche)|(küchen)|(küche)|(höhle)|(höhlen)|(werkstätten)|(werkstatt)|(steinbrüche)|(steinbruch)|(höhle)|(höhlen)|(saegewerk)|(sägewerke)|(sägewerk)|(saegewerke)|(hafen)|(häfen)|(höhle)|(höhlen)|(schiffswerft)|(schiffswerften)|(minen)|(mine)|(höhle)|(höhlen)|(kathedrale)|(kathedralen)|(steuerturm)|(steuertürme)|(sattlerei)|(sattlereien)|(steg)|(stege)|(monument)|(monumente)|(wegweiser)) [a-z0-9]{1,4}([ ]+(\/\/).*)?

^(unterhalte)[n]?([ ]+(\/\/).*)?
^(unterhalte)[n]?( [0-9]+)([ ]+(\/\/).*)?

^(handel)[n]? ((kaufe)[n]?) (")?(|(juwel)|(juwelen)|(weihrauch)|(ballen feinster seide)|(seide)|(balsam)|(oele)|(oel)|(gewuerz)|(gewuerze)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(pelz)|(pelze))(")?([ ]+(\/\/).*)?
^(handel)[n]? ((kaufe)[n]?)( [0-9]+) (")?(|(juwel)|(juwelen)|(weihrauch)|(ballen feinster seide)|(seide)|(balsam)|(oele)|(oel)|(gewuerz)|(gewuerze)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(pelz)|(pelze))(")?([ ]+(\/\/).*)?
^(handel)[n]? ((verkaufe)[n]?) (")?(|(juwel)|(juwelen)|(weihrauch)|(ballen feinster seide)|(seide)|(balsam)|(oele)|(oel)|(gewuerz)|(gewuerze)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(pelz)|(pelze))(")?([ ]+(\/\/).*)?
^(handel)[n]? ((verkaufe)[n]?)( [0-9]+) (")?(|(juwel)|(juwelen)|(weihrauch)|(ballen feinster seide)|(seide)|(balsam)|(oele)|(oel)|(gewuerz)|(gewuerze)|(säcke myrrhe)|(sack myrrhe)|(myrrhe)|(myhrre)|(myrre)|(pelz)|(pelze))(")?([ ]+(\/\/).*)?

^(treibe)[n]?([ ]+(\/\/).*)?
^(treibe)[n]?( [0-9]+)([ ]+(\/\/).*)?

^(beklaue)[n]? [a-z0-9]+([ ]+(\/\/).*)?

^nach( +(nw|no|o|so|sw|w|nordwesten|nordosten|osten|suedosten|suedwesten|westen|pause))+([ ]+(\/\/).*)?
^route( +(nw|no|o|so|sw|w|nordwesten|nordosten|osten|suedosten|suedwesten|westen|pause))+([ ]+(\/\/).*)?
^nach(( \([-+]?[0-9]+ [-+]?[0-9]+\))|( pause))+([ ]+(\/\/).*)?
^route(( \([-+]?[0-9]+ [-+]?[0-9]+\))|( pause))+([ ]+(\/\/).*)?

^folge(n)? (einheit) [a-z0-9]{1,4}([ ]+(\/\/).*)?
^folge(n)? (einheit) (temp) [a-z0-9]{1,4}([ ]+(\/\/).*)?
^folge(n)? (schiff) [a-z0-9]{1,4}([ ]+(\/\/).*)?
^@?(bewache)[n]?([ ]+(\/\/).*)?

^@?(botschaft temp)[a-z0-9]{1,4}( ).*
^@?(botschaft )[a-z0-9]{1,4}( ).*
^@?(botschaft einheit temp)[a-z0-9]{1,4}( ).*
^@?(botschaft einheit )[a-z0-9]{1,4}( ).*
^@?(botschaft )((partei)|(volk))( )[a-z0-9]{1,4}( ).*
^@?(botschaft )(an )?((region)|(alle))( ).*

^@?(bestätigt|bestaetigt) [1-9]{1}[0-9]{0,5}([ ]+(\/\/).*)?
^@?(bestätigt|bestaetigt) bis [1-9]{1}[0-9]{0,5}([ ]+(\/\/).*)?

^(faulenze)[n]?([ ]+(\/\/).*)?
^@?(default) .+
