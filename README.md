# ChunkyOffline

Generates chunks while your server is empty, using [Chunky](https://github.com/pop4959/Chunky).

Works on Paper, Folia, Purpur, and Spigot.

## Requirements

- Paper 26.3+ (Folia supported)
- Chunky 1.5.3+
- Java 25+

## Install

1. Drop the JAR into `plugins/`.
2. Restart the server.
3. Run `/chunkyoffline start`.

## Commands

| Command | What it does | Permission |
|---|---|---|
| `/co start` | Start generating chunks | `chunkyoffline.admin.start` |
| `/co pause` | Pause generation | `chunkyoffline.admin.pause` |
| `/co resume` | Resume generation | `chunkyoffline.admin.resume` |
| `/co cancel` | Cancel generation | `chunkyoffline.admin.cancel` |
| `/co config` | Change settings | `chunkyoffline.admin.config` |

`/chunkyoffline` works too. All permissions are OP by default.

## Settings

The plugin creates `plugins/ChunkyOffline/config.yml`:

```yaml
chunk-generation:
  radius: 10000
  center:
    x: 0
    z: 0
  global-messages: true
```

You can also change radius and center with `/co config set <radius|center-x|center-z> <value>` and read them back with `/co config get <option>`.

## Build

```bash
./gradlew build
```

The JAR ends up in `build/libs/`.

## License

See [LICENSE](LICENSE).
