# xfsm-java


```gradle
repositories {
    jcenter()
    maven {
        url "http://dl.bintray.com/sng2c/maven"
    }
}

dependencies {
    compile 'com.mabook:xfsm:1.1.6'
}
```

## plantuml format

```plantuml
@startuml
State HOME
HOME : in 'SAY_I_AM_BACK'
HOME : out 'SAY_I_WILL_BE_BACK'

State SCHOOL
SCHOOL : in 'YO_FRIENDS'
SCHOOL : out 'BYE_FRIENDS'

[*] --> HOME : event '__init__'
HOME --> SCHOOL : event 'EV_AM8'
SCHOOL --> HOME : event 'EV_PM7' do 'HAVE_DINNER'
@enduml
```

<img src="http://www.plantuml.com/plantuml/img/PP2n2eCm48RtFCLjWO9pIuTYr9I4gYa6B5BA8PX1Q2EqiS_Vs4OTkjmDtyztFnU_NFjqu-DEj6kT0Q4AJgO1U-WjKCqQbCWAJ5XweZCPHZUZYynpJ7ZWmQ9JeLHEVYkO6eN7Il8oqtG5Nr7Iy1MHw-O6KNI0SCgyZVKPnDxs3Z5Kc0AB94HuxNlk5lc_mmVEnPP2Dm0LhEQOoRBa5IMniPr_mGS0">

## json format

```json
{
    "initialEvent": "__init__",
    "states": {
        "HOME": {
            "name": "HOME",
            "onEnter": "SAY_I_AM_BACK",
            "onExit": "SAY_I_WILL_BE_BACK"
        },
        "SCHOOL": {
            "name": "SCHOOL",
            "onEnter": "YO_FRIENDS",
            "onExit": "BYE_FRIENDS"
        }
    },
    "transitions": {
        "EV_AM8@HOME": {
            "event": "EV_AM8",
            "fromStateName": "HOME",
            "toStateName": "SCHOOL"
        },
        "__init__": {
            "event": "__init__",
            "toStateName": "HOME"
        },
        "EV_PM7@SCHOOL": {
            "event": "EV_PM7",
            "fromStateName": "SCHOOL",
            "toStateName": "HOME",
            "onTransition": "HAVE_DINNER"
        }
    }
}
```

