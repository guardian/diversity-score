### How to use

This service can be reached at 52.16.180.145

Q: do we want this to be public? What should the authorisation control be?
Q: shall we give this a friendly DNS?


#### Endpoints
curl -X POST /score -d "Your text"

Successful response
{

}

### JSON Schema

```json
{
  "type": "object",
  "properties": {
    "score": {
      "type": "number"
    },
    "message": {
      "type": "string",
      "enum": [
        "No entities detected, not possible to calculate a score"
      ]
    },
    "entities": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "name": {
            "type": "string"
          },
          "gender": {
            "type": "string",
            "enum": [
              "NonMale",
              "Male"
            ]
          }
        }
      }
    },
    "required": [
      "entities"
    ]
  }
}
```
