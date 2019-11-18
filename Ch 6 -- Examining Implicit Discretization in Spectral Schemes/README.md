## Overview of Supplemental Materials

All supplemental materials for the publication "Examining Explicit Discretization in Spectral Schemes" have been combined into a single compressed file for convenience. In addition to this README, these supplemental materials include:

- Experiment Demos *(folder)*
- Experimental Data *(folder)*
- Publication Figures *(folder)*
- Statistical Analysis *(folder)*
- Supplement *(pdf)*
- Visual Analysis *(folder)*

We provide brief description of each below.

### Experiment Demos

This folder contains runnable versions of both our training and study modules, in respective sub-folders. These folders contain binaries for Mac and Windows as well as the original source Processing sketches. The Processing code has been tested in both Processing 3.2.3 and the latest updated version (3.3.7). Processing can be downloaded from [https://processing.org/download/](https://processing.org/download/). 

*A note for Mac users:* Due to space limitations, the Mac binary does not include a copy of the Java Runtime Environment and, instead, expects a system install of Java. If you do not have Java installed on your system, you can either install Java or follow the instructions outlined below for running the original Processing source, since Processing contains a local Java Runtime Environment.

For those interested in running the source sketches, the sketches requires that the both the giCentreUtils and BlobDetection libraries be installed. To install these libraries open Processing and go to `Sketch -> Import Library... -> Add Library` in the menu bar. This should launch a Contribution Manager window. Use the filter in the top right to to search for `gicentreUtils`. Select the library from the list and click the `Install` button on the bottom right of the window. Repeat this process to search for and install the 'blobDetection' library.

You should now be able to run either the training or study sketches. Open the sketch by navigating to the sketch folder and double clicking on one of the `.pde` files or by using the `File -> Open` dialog in Processing's menu bar to locate the associated `.pde` file. These files can be found in either the `Experiment Demos/Training/processing.sketch/RCM_Training/` folder or the `Experiment Demos/Study/processing.sketch/RCM_Study/` folder. Once the sketch is open, you can run the sketch in Present mode by selecting `Sketch -> Present` from the menu bar.

These modules were designed to be run on 1920x1200 resolution monitors. It should also run fine at 1680×1050 resolution, but has not been tested on smaller screens. For accurate color reproduction you should ensure that your system color manager is set to use sRGB and that your monitor is calibrated. If, however, you are simply trying to better understand the experimental interactions, what our training looked like, etc., then that is probably overkill.

### Experimental Data

This folder contains the full cleaned data collected from from the study (including outliers). The `{boundary,category}` prefixes relate to the responses collected for our two instruction conditions.

- `{boundary,category}Records.csv` contains the actual collected responses from the main study module.
- `{boundary,category}Survey.csv` contains the responses collected from our post-study survey
- `{boundary,category}Derived.csv` contains a variety of data fields derived from either the study responses (e.g., number of delimiters placed) or the survey responses (e.g., response strategy at Mach bands)

### Publication Figures

This folder contains high-resolution, ICC-profile color managed versions of all of the figures included in the publication. There is a separate folder or file for each figure, named to correspond to the figure numbers in the paper.

### Statistical Analysis

This folder contains two documents:

- `ExaminingEV19Stats.Rmd` is an RMarkdown file containing the R code used to run the stats described in Section 5.1 of the paper and Appendix D of the supplemental pdf.
- `ExaminingEV19Stats.html` contains the model outputs for the statistical tests described in Section 5.1 of the paper and Appendix D of the supplemental pdf.

### Supplement

Supplement.pdf includes details and results omitted from the original publication. The contents have been separated into four appendices, each summarized below:

- **Appendix A**: Defining the Kindlmann Color Map
	- An expanded discussion of our decision to use the Kindlmann color map in the study, as referenced in Section 4.3 of the paper.
- **Appendix B**: Deriving Indicators
	- A detailed overview of the numerical methods used to derive the indicator sets used in our analyses, expanding on discussions from Sections 5.2
- **Appendix C**: Kernel Density Estimates and Dependent Data
	- An examination of various potential issues with kernel density estimation (KDE) and how we have addressed them in this work.
- **Appendix D**: Expanded Descriptive Statistical Analysis
	- An expanded NHST-style description of the statistical analyses conducted on number of delimiters participants counted and placed.
- **Appendix E**: Expanded Results Overview
	- The full set of participants’ boundary placement results shown using the visual analysis methods employed in Sections 5 and 6 of the paper, along with an expanded discussion of the grayscale results.

### Visual Analysis

This folder contains:

- `ExaminingEV19.ipynb`: a jupyter notebook with the code to generate the various static and interactive plots mentioned in both the paper and the Supplement.pdf.
- `ConvenienceClasses.py`: a set of convenience classes used by the jupyter notebook
- `data/`: a folder with additional resources needed by the jupyter notebook
-  `environment.yml`: the specification for a conda environment containing all the dependencies needed to run the jupyter notebook

To aid in reproducibility, we have made an interactive version of the `ExaminingEV19.ipynb` notebook publicly available as part of a executable Binder environment on [myBinder.org](https://mybinder.readthedocs.io/en/latest/).
 
[![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/samquinan/ExaminingEV19/master?filepath=ExaminingEV19.ipynb)

Since the Binder instance being hosted at myBinder.org is part of a research pilot, we have also included the specification for an Anaconda environment with all the relevant dependencies needed to run the jupyter notebook locally. Anaconda is a python and R distribution, with installation and package management tools and a complete overview of `conda` environments can be found in Anaconda's [documentation](https://conda.io/docs/user-guide/tasks/manage-environments.html). Below we outline the basic steps required to set up the included environment. These directions, tested on both macOS and Linux, assume that you have a working version of Anaconda [installed](https://docs.anaconda.com/anaconda/install/). If you are installing Anaconda for the first time, we also recommend familiarizing yourself with the steps in the [user guide](https://conda.io/docs/user-guide/getting-started.html) for starting and managing `conda` environments.

####  Running the Jupyter Notebook Locally

- Navigate to included `Visual Analysis/` directory in your terminal (macOS and Linux) or in Anaconda Prompt (Windows).
- Run `conda env create -f environment.yml` to create the environment from the provided `environment.yml` file.
- Active the Examining2018-Env environment using the command:
	- `activate ExaminingEV19-Env` (Windows)
	- `source activate ExaminingEV19-Env` (macOS and Linux)
- You can verify that the new environment was installed correctly by running `conda env list`. You should see a list of environments with an asterisk (*) next to the `ExaminingEV19-Env` environment.
- From the same `Visual Analysis/` directory run the `jupyter notebook` command. This should launch a browser window containing the contents of the `Visual Analysis/` directory.
- Click on the `ExaminingEV19.ipynb`. You now have a running version of the jupyter notebook. Running the cells in order should allow one to reproduce the various static and interactive plots used in our analysis.
- To stop serving the jupyter notebook, use `control-C` to stop the server and shut down all kernels.
- When you are done you can follow the documentation's steps for [deactivating](https://conda.io/docs/user-guide/tasks/manage-environments.html#deactivating-an-environment) and/or [removing](https://conda.io/docs/user-guide/tasks/manage-environments.html#removing-an-environment) the environment.
