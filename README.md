# Subway Simulation

## Presentation

### Install Dependencies

```
virtualenv .env
. .env/bin/activate
pip install -r requirements.txt
```

### Editing

```
cd doc
jupyter notebook
```

### Publishing

Display the slides in the browser

```
cd docs
jupyter nbconvert presentation.ipynb --to slides --post serve
```

#### Saving as PDF

Then change the url in the browser to http://127.0.0.1:8000/presentation.slides.html?print-pdf#/
and print the page as a pdf. You should see the individual slides (only works properly in Chrome 
and Chromium).
