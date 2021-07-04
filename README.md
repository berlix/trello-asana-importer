# Trello-Asana Importer

This is a tool I wrote for myself to migrate [Trello](https://trello.com) boards to [Asana](https://asana.com).
The motivation was that the only available commercial solution did not migrate archived cards.

**The code is not production quality - use at your own risk.** Do with it whatever you like.

## Usage

In Asana, create an access token.

In Trello, download JSON exports of the boards you would like to import into Asana.

Then, run `pro.felixo.importer.MainKt` with two command-line arguments:
1. your Asana access token;
2. the path of the directory containing (only) the exported Trello JSON files.

## Behaviour

The tool migrates Trello entities to Asana as follows:

Trello | Asana
--- | ---
Board | Project (same name, description; board view by default; will abort if project of same name already exists)
Label | Tag (same name, similar colour)
List (incl. archived) | Section (same order and name)
Card (incl. archived) | Task (same order, name, and description; archived card -> completed task; no assignment)
Card comment | Task comment (same order; original timestamp in text; commenter name not included)
Checklist item | Sub-task (same order, name, and completion state)
Attachment | Attachment (same name and content)

## Known issues

I made this tool for a single, specific use case that I had, and it was good enough. You may notice
the following flaws, however:

- It will not work if the access token has access to more than one organisation or more than one team.
- Created Asana boards have an empty "Untitled section".
- Attachment file names end up URL-encoded.
- Not all label colours are translated.
- If a Trello JSON export contains an action type unknown to the tool, an exception will be thrown. See
  class `ActionType` in `BoardState.kt`.
