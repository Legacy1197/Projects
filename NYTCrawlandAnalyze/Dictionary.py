import numpy as np
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.preprocessing import StandardScaler
from sklearn.neural_network import MLPClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.manifold import TSNE
import matplotlib.colors
import matplotlib.pyplot as plt
from sklearn.feature_extraction.text import CountVectorizer
import json

# Plotting method
def plot_embedding(X, y, title=None):
    x_min, x_max = np.min(X, 0), np.max(X, 0)
    X = (X - x_min) / (x_max - x_min)

    cm = plt.get_cmap('gist_rainbow')
    norm = matplotlib.colors.Normalize(vmin=00.0, vmax=21.0)

    plt.figure()
    plt.scatter(X[:, 0], X[:, 1], color=cm(norm(y)))
    plt.xticks([])
    plt.yticks([])
    if title is not None:
        plt.title(title)
    plt.show()


def unique_labels(labels):
    uniquelist = []
    for x in labels:
        if not uniquelist:
            uniquelist.append(x)
        else:
            check = False
            for y in uniquelist:
                if x == y:
                    check = True
            if check is False:
                uniquelist.append(x)
    return uniquelist


def int_label_converter(old_labels, unique):
    int_labels = np.empty_like(old_labels, np.int64)
    for i in range(len(unique)):
        for j in range(len(old_labels)):
            if old_labels[j] == unique[i]:
                int_labels[j] = i
    return int_labels


in_path = "NewYorkTimesClean.jsonl"
data_labels = []
sentences = []
vectorizer = CountVectorizer(analyzer="word", tokenizer=None, preprocessor=None, stop_words=None, max_features=500)

with open(in_path) as f:
    for i, line in enumerate(f):
        if i % 15 is 0:
            data = json.loads(line)
            section = data['section']

            keywords = data['keywords']
            lead_paragraph = data['lead_paragraph']

            if lead_paragraph is not None:
                sentence = lead_paragraph
                temp = sentence.split(" ")
                temp = [each.strip(',').lower() for each in temp]
                temp = [each.strip('.').lower() for each in temp]
                temp = [each.strip(')').lower() for each in temp]
                temp = [each.strip('(').lower() for each in temp]
                for words in temp:
                    sentence += ' ' + words
            else:
                sentence = ''

            # add keywords
            if keywords is not None:
                for each in keywords:
                    tmp = each['value']
                    tmp = tmp.split(" ")
                    tmp = [each.strip(',').lower() for each in tmp]
                    tmp = [each.strip('.').lower() for each in tmp]
                    tmp = [each.strip(')').lower() for each in tmp]
                    tmp = [each.strip('(').lower() for each in tmp]
                    tmp = [each.strip('\'').lower() for each in tmp]
                    for word in tmp:
                        sentence += ' ' + word
            if sentence is not ' ':
                data_labels.append(section)
                sentences.append(sentence)

train_features = vectorizer.fit_transform(sentences)
features_temp = []
for each in sentences:
    features_temp.append(vectorizer.transform([each]).toarray())

data_features = []
for each in features_temp:
    data_features.append(each[0])

# Preprocess dataset, split into training and test part
X = data_features
y = data_labels
X = StandardScaler().fit_transform(X)
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=.4, random_state=42)

#clf = MLPClassifier()
#clf.fit(X_train, y_train)
#score = clf.score(X_test, y_test)
#cv_score_2 = cross_val_score(clf, X, y, cv=2)
#cv_score_4 = cross_val_score(clf, X, y, cv=4)
#print('Multiple Layer Perceptron')
#print('Accuracy: ', score)
#print('Cross validation score k=2:', cv_score_2)
#print('Cross validation score k=4:', cv_score_4)

#result_labels = clf.predict(X_test)
#converted_labels = int_label_converter(result_labels, unique_labels(result_labels))
# Reduce the data to 2-D space
#embeddedSpace = TSNE(n_components=2).fit_transform(X_test)
# Visualization
#plot_embedding(embeddedSpace, converted_labels, 't-SNE MultiLayer Perceptron')


clf = RandomForestClassifier()
clf.fit(X_train, y_train)
score = clf.score(X_test, y_test)
cv_score_2 = cross_val_score(clf, X, y, cv=2)
cv_score_4 = cross_val_score(clf, X, y, cv=4)
print('Random Forest')
print('Accuracy: ', score)
print('Cross validation score k=2:', cv_score_2)
print('Cross validation score k=4:', cv_score_4)
result_labels = clf.predict(X_test)
converted_labels = int_label_converter(result_labels, unique_labels(result_labels))
print(converted_labels.shape)
# Reduce the data to 2-D space
embeddedSpace = TSNE(n_components=2).fit_transform(X_test)
# Visualization
plot_embedding(embeddedSpace, converted_labels, 't-SNE Random Forest')


#clf = KNeighborsClassifier(n_neighbors=5)
#clf.fit(X_train, y_train)
#score = clf.score(X_test, y_test)
#cv_score_2 = cross_val_score(clf, X, y, cv=2)
#cv_score_4 = cross_val_score(clf, X, y, cv=4)
#print('K-Nearest Neighbors')
#print('Accuracy: ', score)
#print('Cross validation score k=2:', cv_score_2)
#print('Cross validation score k=4:', cv_score_4)

#result_labels = clf.predict(X_test)
#converted_labels = int_label_converter(result_labels, unique_labels(result_labels))
#print(converted_labels.shape)
#Reduce the data to 2-D space
#embeddedSpace = TSNE(n_components=2).fit_transform(X_test)
#Visualization
#plot_embedding(embeddedSpace, converted_labels, 't-SNE K-Nearest Neighbor')
